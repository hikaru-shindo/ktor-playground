package com.example.shop

import io.github.databob.Databob

fun createProduct(databob: Databob) = Product(id = databob.mk(), name = databob.mk(), price = createPrice(databob))
fun createPrice(databob: Databob) = Price(value = databob.mk(), currencyCode = databob.mk())
