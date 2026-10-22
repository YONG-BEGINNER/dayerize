package app.dayerize.core

import java.time.LocalDate
import java.math.BigDecimal

data class LocalSearchFilter(val text:String,val category:String?,val from:LocalDate?,val to:LocalDate?,val below:BigDecimal?,val currency:String,val status:Status?) {
    fun matches(a:Asset,rates:List<FxRate>,today:LocalDate):Boolean {
        if(category!=null && a.category!=category || status!=null && a.status!=status)return false
        val bought=LocalDate.parse(a.purchaseDate)
        if(from!=null && bought<from || to!=null && bought>to)return false
        if(below!=null) {
            val value=if(a.purchase.currency==currency)a.purchase.decimal() else {
                val rate=rates.filter{it.from==a.purchase.currency && it.to==currency && it.date<=today}.maxByOrNull{it.date} ?: return false
                a.purchase.decimal()*rate.rate
            }
            if(value>=below)return false
        }
        return true
    }
}
object LocalSearch {
    fun parse(input:String,today:LocalDate,currency:String,categories:List<String>):LocalSearchFilter {
        var text=input.trim();var from:LocalDate?=null;var to:LocalDate?=null;var below:BigDecimal?=null;var status:Status?=null;var category:String?=null
        val lastYear=Regex("(?i)\\b(?:bought\\s+)?last year\\b|去年|tahun lepas")
        if(lastYear.containsMatchIn(text)){from=LocalDate.of(today.year-1,1,1);to=LocalDate.of(today.year-1,12,31);text=text.replace(lastYear,"")}
        val thisYear=Regex("(?i)\\b(?:bought\\s+)?this year\\b|今年|tahun ini")
        if(from==null && thisYear.containsMatchIn(text)){from=LocalDate.of(today.year,1,1);to=today;text=text.replace(thisYear,"")}
        val price=Regex("(?i)(?:\\bunder\\s+|低于\\s*|低於\\s*|di bawah\\s+)([0-9]+(?:\\.[0-9]+)?)")
        price.find(text)?.let{below=it.groupValues[1].toBigDecimalOrNull();text=text.replaceRange(it.range,"")}
        val states=listOf(Status.ACTIVE to "active|服役中|aktif",Status.RETIRED to "retired|已退役|bersara",Status.SOLD to "sold|已售出|dijual")
        for((value,words) in states){val regex=Regex("(?i)(?<![\\p{L}])(?:$words)(?![\\p{L}])");if(regex.containsMatchIn(text)){status=value;text=text.replace(regex,"");break}}
        for(c in categories.sortedByDescending{it.length}) {
            val regex=Regex("(?i)(?<![\\p{L}])"+Regex.escape(c)+"(?![\\p{L}])")
            if(regex.containsMatchIn(text)){category=c;text=text.replace(regex,"");break}
        }
        if(from!=null || below!=null || category!=null || status!=null)text=text.replace(Regex("(?i)^\\s*(?:show|find|显示|顯示|cari)\\s*"),"")
        return LocalSearchFilter(text.trim(),category,from,to,below,currency,status)
    }
}
