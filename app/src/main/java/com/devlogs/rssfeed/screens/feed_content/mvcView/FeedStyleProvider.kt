package com.devlogs.rssfeed.screens.feed_content.mvcView

import com.devlogs.rssfeed.screens.feed_content.presentable_model.FeedPresentableModel

class FeedStyleProvider {
    private val rawCssStyle = """
                * {
                    background: rgba(26, 26, 26, 1);
                    box-sizing: border-box;
                }
                
                body {
                    font-size: 14px;
                }
                
                a {
        	        color: white
                }
                
                img {
                    width: auto;
                    height: auto;
                    margin-top: 20px;
                    max-width: 100%;
                    padding-bottom: 20px
                }
                
                .v3_805 {
                    margin-top: 20px;
                }
                
                .v3_844 {
                    width: 98%;
                    color: rgba(255, 255, 255, 1);
                    top: 101px;
                    font-family: Public Sans;
                    font-weight: Bold;
                    font-size: 25px;
                    opacity: 1;
                    text-align: left;
                }
                
                .v3_848 {
                    width: 99%;
                    color: rgba(206, 206, 206, 1);
                    top: 44px;
                    font-family: Public Sans;
                    font-weight: Regular;
                    font-size: 14px;
                    opacity: 1;
                    text-align: left;
                }
                
                .v3_847 {
                    width: 99%;
                    color: rgba(206, 206, 206, 1);
                    top: 26px;
                    font-family: Public Sans;
                    font-weight: Regular;
                    font-size: 14px;
                    opacity: 1;
                    text-align: left;
                }
                
                .v3_849 {
                    margin-bottom: 70px width: 99%;
                    padding-top: 25px;
                    color: rgba(187, 187, 187, 1);
                    top: 190px;
                    font-family: Public Sans;
                    font-weight: Regular;
                    font-size: 16px;
                    opacity: 1;
                    text-align: left;
                }
                
                .visitOutline {
                    width: 100%;
                    margin-top: 30px;
                    border: 1px solid rgba(142, 139, 139, 1);
                    color: white;
                    padding: 20px;
                    text-align: center;
                    border-top-left-radius: 5px;
                    border-top-right-radius: 5px;
                    border-bottom-left-radius: 5px;
                    border-bottom-right-radius: 5px;
                }
                
                .aref {
                    color: white
                }
        
    """.trimIndent()

    private val rawHtml = """
        <html>
            <head>
                <link href="https://fonts.googleapis.com/css?family=Public+Sans&display=swap" rel="stylesheet" />
                <link href="./main.css" rel="stylesheet" /> </head>
            
            <body> 
                <span class="v3_847">_author</span> <br/> 
                <span class="v3_848">_pubDate</span> 
                <span class="v3_849"><br/>         
                    <div class="v3_805">
                    <span class="v3_844">_feedTitle</span> 
                </span> 
                <span> _content </span>
                <div class="visitOutline">
                    <span><a class="aref" href="_feedUrl">Visit Website</a></span> 
                </div>
            </body>
        </html>
    """.trimIndent()

    private val feed: FeedPresentableModel;
    private var readyHtml: String

    constructor(feed: FeedPresentableModel) {
        this.feed = feed
        readyHtml = rawHtml.replace("_content", feed.feedContent)
        readyHtml = readyHtml.replace("_feedUrl", feed.feedUrl)
        readyHtml = readyHtml.replace("_feedTitle", feed.feedTitle)
        readyHtml = readyHtml.replace("_pubDate", feed.pubDate)
        readyHtml = readyHtml.replace("_author", feed.author)
    }

    fun getHtml (): String {
        return readyHtml
    }

    fun getCss () : String {
        return rawCssStyle
    }

}