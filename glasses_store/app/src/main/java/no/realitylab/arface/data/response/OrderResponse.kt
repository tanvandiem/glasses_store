package no.realitylab.arface.data.response

import no.realitylab.arface.model.Order
import no.realitylab.arface.model.Product
import no.realitylab.arface.model.VariationDetails

data class OrderResponse(
    val data: OrderData
)

data class OrderData(
    val TotalOrders: Int,
    val orders: List<Order>
)




