package com.afjltd.tracking.utils

import java.util.*

fun String.findFloat(): ArrayList<Float> {
    //get digits from result
    if (this.isEmpty()) return ArrayList<Float>()
    val originalResult = ArrayList<Float>()
    val matchedResults = Regex(pattern = "[+-]?([0-9]*[.])?[0-9]+").findAll(this)
    for (txt in matchedResults) {
        if (txt.value.isFloatAndWhole()) originalResult.add(txt.value.toFloat())
    }
    return originalResult
}

fun String.firstLine(): String {
    if (this.isEmpty()) return ""
    return this.split("\n").get(0)
}


val datePattern =
    ("^((2000|2400|2800|(19|2[0-9])(0[48]|[2468][048]|[13579][26]))-02-29)$"
            + "|^(((19|2[0-9])[0-9]{2})-02-(0[1-9]|1[0-9]|2[0-8]))$"
            + "|^(((19|2[0-9])[0-9]{2})-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))$"
            + "|^(((19|2[0-9])[0-9]{2})-(0[469]|11)-(0[1-9]|[12][0-9]|30))$").toRegex()

fun String.isDateString() = this.contains(datePattern)


val totalSubPrice = """(?i)\b(grand total|total|sub total|subtotal|vat|gst)\b""".toRegex()
fun String.isTotaPriceandTax() = this.contains(totalSubPrice)

val itemPrice= "[+-]?([0-9]*[.])?[0-9]+".toRegex()
fun String.isItemPrices() = this.contains(itemPrice)


fun String.isFloatAndWhole() = this.matches("\\d*\\.\\d*".toRegex())

fun String.isFloat(): Boolean {
    try {
        val d = java.lang.Double.valueOf(this)
        return d != d.toInt().toDouble()
    } catch (e: Exception) {
        return false
    }
}


fun getTuples(text: String): List<Pair<Double, String>> {
    var products = mutableListOf<Pair<Double, String>>()
    var produs_crt = 0
    var pret_crt = 0
    var nume_crt = 0
    var started =
        false  // all the text from the upper part of the receipt is unnecessary and it should be skipped
    val lines = text.split("\n")
    for (line in lines) {

        if (line.lowercase().contains("total")) {
            AFJUtils.writeLogs("Total Word =${line}")
        }

        if ("total" == line.lowercase(Locale.getDefault()) || "*" in line.lowercase(Locale.getDefault())
        ) {
            break
        }

        if (started
            && !line[0].isDigit()
            && "discount" !in line.lowercase(Locale.getDefault())
            && "total" !in line.lowercase(Locale.getDefault())
            && "vat" != line.lowercase(Locale.getDefault())
            && "lel" != line.lowercase(Locale.getDefault())
        ) {  // the text recognizer might confuse i for l

            if (nume_crt == produs_crt) {
                products.add(Pair(0.toDouble(), line))
                produs_crt += 1
                nume_crt += 1
            } else {
                products[nume_crt] = Pair(products[nume_crt].first, line)
                nume_crt += 1
            }
        } else {
            val trimmedLine = line.drop(line.lowercase(Locale.getDefault()).indexOf("x ") + 2)
            if (trimmedLine.isEmpty()) {
                continue
            }
            val words = trimmedLine.split(' ')
            if (words.isNotEmpty()) {
                if (words[0].toDoubleOrNull() != null) {
                    val nr = words[0].toDouble()
                    if (pret_crt == produs_crt) {
                        products.add(Pair(nr, ""))
                        produs_crt += 1
                        pret_crt += 1
                    } else {
                        products[pret_crt] = Pair(nr, products[pret_crt].second)
                        pret_crt += 1
                    }
                    started = true
                }
            }
        }
    }
    return products
}
