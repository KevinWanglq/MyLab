package com.hyls.crawle;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupTest {
	public static void main(String[] args) throws IOException {
        //��ȡ�༭�Ƽ�ҳ
        Document document=Jsoup.connect("https://www.zhihu.com/explore/recommendations")
                //ģ���������
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Safari/537.36")
                .get();
        Element main=document.getElementById("zh-recommend-list-full");
        Elements url=main.select("div").select("div:nth-child(2)")
                .select("h2").select("a[class=question_link]");
        for(Element question:url){
            //���href���ֵ������ҳ��ÿ����ע���������
            String URL=question.attr("abs:href");
            //������������ָ���ҳ��
            Document document2=Jsoup.connect(URL)
            		.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Safari/537.36")
                    //.userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")
                    .get();
            //����
            Elements title=document2.select("#root").select("div").select("main").select("div").select("div:nth-child(11)")
            		.select("div.QuestionHeader").select("div.QuestionHeader-content").select("div.QuestionHeader-main").select("h1");
            //Elements title=document2.select("#zh-question-title").select("h2").select("a");
            //��������
            Elements detail=document2.select("#root").select("div").select("main").select("div").select("div:nth-child(11)")
            		.select("div.QuestionHeader").select("div.QuestionHeader-content").select("div.QuestionHeader-main")
            		.select("div:nth-child(3)").select("div").select("div").select("div").select("span").select("p");
            //�ش�
            Elements answer=document2.select("#zh-question-answer-wrap")
                    .select("div.zm-item-rich-text.expandable.js-collapse-body")
                    .select("div.zm-editable-content.clearfix");
            System.out.println("\n"+"���ӣ�"+URL
                    +"\n"+"���⣺"+title.text()
                    +"\n"+"����������"+detail.text()
                    +"\n"+"�ش�"+answer.text());
        }   
    }

}
