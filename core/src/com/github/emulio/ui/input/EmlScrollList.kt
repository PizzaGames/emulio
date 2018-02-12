package com.github.emulio.ui.input

class EmlScrollList<T>(scroll: EmlScroll, itemsList: EmlList)
{
    private val list = itemsList
    private val scroll = scroll

    fun scroll(amount: Int) {
        val nextIndex = (this.list.selectedIndex + amount)

        list.selectedIndex = nextIndex.coerceIn(0 until  list.size.coerceAtLeast(1))

        val scrollTopIndex = calcScrollTopIndex(list.selectedIndex,scroll.size)
        scroll.setScrollTop(scrollTopIndex)
    }

    private fun calcScrollTopIndex(selectionIndex: Int, scrollSize: Int): Int {
        val scrollTopMax = (list.size - scrollSize).coerceAtLeast(0)

        val scrollCenterOffset = (scrollSize/2).coerceAtLeast(0)
        val scrollCenterMax =  (list.size - scrollCenterOffset).coerceAtLeast(0)

        if (selectionIndex < scrollCenterOffset) {
            return 0
        }

        if (selectionIndex > scrollCenterMax) {
            return scrollTopMax
        }

        return (selectionIndex - scrollCenterOffset).coerceAtLeast(0)
    }
}

interface EmlList {
    var selectedIndex: Int
    val size: Int
}

interface EmlScroll {
    val size: Int
    fun setScrollTop(top: Int)
}

