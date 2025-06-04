package com.example.readingbooks.data.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "docs", strict = false)
data class NlBookResponse(
    @field:ElementList(entry = "doc", inline = true, required = false)
    var doc: List<NlBookItem>? = null
)

@Root(name = "doc", strict = false)
data class NlBookItem(
    @field:Element(name = "page_info", required = false)
    var pageCount: String? = null,

    @field:Element(name = "title_info", required = false)
    var title: String? = null
)
