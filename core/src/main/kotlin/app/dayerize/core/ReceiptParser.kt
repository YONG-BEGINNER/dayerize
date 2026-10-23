package app.dayerize.core

import java.time.LocalDate
import java.util.Currency

/** Unverified local OCR suggestions, never an instruction to save or overwrite an asset. */
data class ReceiptAmount(val value:String,val line:String,val total:Boolean)
data class ReceiptCandidates(val amounts:List<ReceiptAmount>,val currencies:Set<String>,val dates:List<String>,val retailer:String?) {
    val suggestedAmount:String? get()=amounts.filter{it.total}.singleOrNull()?.value?.takeIf{currencies.size<=1}
}

object ReceiptParser {
    private val datePattern=Regex("(?<!\\d)(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{4})(?!\\d)")
    private val number=Regex("(?<![\\p{L}\\d.,])\\d+(?:[.,]\\d+)*(?![\\p{L}\\d.,])")
    private val total=Regex("(?i)(\\b(grand\\s+total|total|amount\\s+due|balance\\s+due|jumlah|jumlah\\s+besar)\\b|合计|合計|总计|總計|应付|應付)")
    private val excluded=Regex("(?i)(\\b(sub\\s*total|tax|cash|tender|change|discount)\\b|小计|小計|找零|税|稅)")
    private val supported=Currency.getAvailableCurrencies().filter{it.defaultFractionDigits in 0..3}.map{it.currencyCode}.toSet()
    fun parse(text:String):ReceiptCandidates {
        require(text.length<=100000){"Receipt too long"}
        val lines=text.lines().map(String::trim).filter(String::isNotEmpty).take(1000)
        val currencies=linkedSetOf<String>();val dates=linkedSetOf<String>();val amounts=mutableListOf<ReceiptAmount>()
        lines.forEach{line->
            Regex("(?<![A-Za-z])[A-Z]{3}(?![A-Za-z])").findAll(line).filter{it.value in supported}.forEach { code->
                val before=line.substring(0,code.range.first).trimEnd()
                val after=line.substring(code.range.last+1).trimStart()
                val beforeMoney=Regex("""^[:(−-]?\s*\d""").containsMatchIn(after)
                val afterMoney=Regex("""\d[)]?$""").containsMatchIn(before)
                val currencyField=Regex("""(?i)^(currency|币种|幣別)\s*:?$""").matches(before)
                if(beforeMoney || afterMoney || currencyField)currencies+=code.value
            }
            if(Regex("(?i)(?<![a-z])RM(?=\\s|\\d|$)").containsMatchIn(line))currencies+="MYR"
            if('€' in line)currencies+="EUR"
            if('£' in line)currencies+="GBP"
            if('¥' in line || '￥' in line){if(!Regex("\\b(CNY|JPY)\\b").containsMatchIn(line)){currencies+="CNY";currencies+="JPY"}}
            datePattern.findAll(line).forEach{m->
                val parts=m.value.split('-','/','.').map(String::toInt)
                fun add(y:Int,mn:Int,d:Int){if(y in 1900..2200)runCatching{LocalDate.of(y,mn,d).toString()}.getOrNull()?.let(dates::add)}
                if(parts[0]>=1900)add(parts[0],parts[1],parts[2])
                else {add(parts[2],parts[1],parts[0]);if(parts[0]!=parts[1])add(parts[2],parts[0],parts[1])}
            }
            val notPrice=Regex("""(?i)(\b(items?|qty|quantity|units?|pieces?|points|savings|refund|credit)\b|件数|件數|数量|數量|积分|積分|退款)""").containsMatchIn(line)
            val isTotal=total.containsMatchIn(line)&&!excluded.containsMatchIn(line)&&!notPrice
            val clean=datePattern.replace(line,"")
            number.findAll(clean).forEach amount@{match->
                val before=clean.substring(0,match.range.first).trimEnd()
                val after=clean.substring(match.range.last+1).trimStart()
                val negative=before.endsWith('-') || before.endsWith('−') || before.endsWith('(') || after.startsWith('-') || after.startsWith('−') || after.startsWith(')') ||
                    Regex("""[-−(]\s*(?:[A-Z]{3}|RM|[$€£¥￥])\s*$""").containsMatchIn(before)
                if(negative || notPrice)return@amount
                val raw=match.value
                if(raw.length>20 || ('.' !in raw && ',' !in raw && !isTotal))return@amount
                val canonical=decimal(raw) ?: return@amount
                amounts+=ReceiptAmount(canonical,line.take(180),isTotal)
            }
        }
        val first=lines.firstOrNull()
        val retailer=first?.takeIf{it.length<=100 && it.any(Char::isLetter) && !it.any(Char::isDigit) && !total.containsMatchIn(it) && !Regex("(?i)^(receipt|tax invoice|invoice|收据|收據|发票|發票)$").matches(it)}
        return ReceiptCandidates(amounts.take(200),currencies,dates.toList(),retailer)
    }
    private fun decimal(raw:String):String? {
        val normalized=when {
            raw.matches(Regex("\\d+"))->raw
            raw.matches(Regex("\\d+[.,]\\d{1,2}"))->raw.replace(',','.')
            raw.matches(Regex("\\d{1,3}(,\\d{3})+\\.\\d{1,3}"))->raw.replace(",","")
            raw.matches(Regex("\\d{1,3}(\\.\\d{3})+,\\d{1,3}"))->raw.replace(".","").replace(',','.')
            else->return null
        }
        return normalized.takeIf{runCatching{it.toBigDecimal().signum()>=0}.getOrDefault(false)}
    }
}
