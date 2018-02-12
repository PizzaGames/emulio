package com.github.emulio.ui.input

import org.junit.jupiter.api.Test
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever


internal class EmlScrollListTest {

    @Test
    fun Scroll_by_zero_should_stay_at_same_index() {
        val mockEmlList: EmlList = mock()
        val emlScroll: EmlScroll = mock()
        whenever(mockEmlList.size).thenReturn(7)
        whenever(mockEmlList.selectedIndex).thenReturn(5)
        whenever(emlScroll.size).thenReturn(3)
        val emlScrolList = EmlScrollList<String>(emlScroll, mockEmlList)

        emlScrolList.scroll(0)

        verify(mockEmlList).selectedIndex = 5
    }

    @Test
    fun Selecting_beyond_list_start_stops_at_start() {
        val mockEmlList: EmlList = mock()
        val emlScroll: EmlScroll = mock()
        whenever(mockEmlList.size).thenReturn(7)
        whenever(mockEmlList.selectedIndex).thenReturn(5)
        whenever(emlScroll.size).thenReturn(3)
        val emlScrolList = EmlScrollList<String>(emlScroll, mockEmlList)

        emlScrolList.scroll(-20)

        verify(mockEmlList).selectedIndex = 0
    }

    @Test
    fun Selecting_beyond_list_end_stops_at_end() {
        val mockEmlList: EmlList = mock()
        val emlScroll: EmlScroll = mock()
        whenever(mockEmlList.size).thenReturn(7)
        whenever(mockEmlList.selectedIndex).thenReturn(5)
        whenever(emlScroll.size).thenReturn(3)
        val emlScrolList = EmlScrollList<String>(emlScroll, mockEmlList)

        emlScrolList.scroll(20)

        verify(mockEmlList).selectedIndex = 6
    }

    @Test
    fun Scrolling_by_less_than_half_the_does_not_move_the_scroll() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(7)
        whenever(list.selectedIndex).thenReturn(1)
        whenever(mockScroll.size).thenReturn(3)
        val emlScrolList = EmlScrollList<String>(mockScroll, list)

        emlScrolList.scroll(1)

        verify(mockScroll).setScrollTop(0)
    }

    @Test
    fun Scrolling_by_more_than_half_the_scroll_size_moves_scroll() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(7)
        whenever(list.selectedIndex).thenReturn(2)
        whenever(mockScroll.size).thenReturn(3)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(2)

        verify(mockScroll).setScrollTop(1)
    }

    @Test
    fun Scrolling_to_the_middle_moves_the_scroll() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(7)
        whenever(list.selectedIndex).thenReturn(4)
        whenever(mockScroll.size).thenReturn(3)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(4)

        verify(mockScroll).setScrollTop(3)
    }

    @Test
    fun Scrolling_to_the_end_stops_the_scroll_at_the_end() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(7)
        whenever(list.selectedIndex).thenReturn(7)
        whenever(mockScroll.size).thenReturn(3)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(7)

        verify(mockScroll).setScrollTop(4)
    }

    @Test
    fun Empty_list_does_not_scroll() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(0)
        whenever(list.selectedIndex).thenReturn(7)
        whenever(mockScroll.size).thenReturn(3)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(7)

        verify(mockScroll).setScrollTop(0)
    }

    @Test
    fun Scroll_greater_than_list_does_not_scroll() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(10)
        whenever(list.selectedIndex).thenReturn(8)
        whenever(mockScroll.size).thenReturn(11)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(7)

        verify(mockScroll).setScrollTop(0)
    }


    @Test
    fun Scrolling_beyond_list_start_stops_at_start() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(10)
        whenever(list.selectedIndex).thenReturn(-20)
        whenever(mockScroll.size).thenReturn(3)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(-20)

        verify(mockScroll).setScrollTop(0)
    }

    @Test
    fun Scrolling_beyond_list_end_stops_at_end() {
        val list: EmlList = mock()
        val mockScroll: EmlScroll = mock()
        whenever(list.size).thenReturn(10)
        whenever(list.selectedIndex).thenReturn(8)
        whenever(mockScroll.size).thenReturn(3)

        val emlScrolList = EmlScrollList<String>(mockScroll, list)
        emlScrolList.scroll(20)

        verify(mockScroll).setScrollTop(7)
    }
}