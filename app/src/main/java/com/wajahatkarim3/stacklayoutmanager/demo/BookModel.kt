package com.wajahatkarim3.stacklayoutmanager.demo

/**
 * Created by wajahat.karim on 02/01/2018.
 */
class BookModel
{
    var bookName = ""
    var coverUrl = ""
    var authorName = ""

    constructor(name: String, author: String, url: String)
    {
        bookName = name
        authorName = author
        coverUrl = url
    }
}