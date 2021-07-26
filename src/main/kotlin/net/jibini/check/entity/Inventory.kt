package net.jibini.check.entity

class Inventory(
    size: Int
)
{
    private val slots = Array(size) { ItemSlot(null, 0) }

    private class ItemSlot(
        var item: Item?,
        var count: Int
    )
}