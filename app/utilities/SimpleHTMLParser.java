/*
 * File: SimpleHTMLParser.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 18
 */
package utilities;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of simple HTML parsing/converting methods.
 *
 * @author Peter Graham
 */
public class SimpleHTMLParser {

    /**
     * Converts the < and > symbols to &lt; and &gt; unless they belong to a 
     * valid tag. Also convert " to &quot;.
     *
     * @param htmlString the String containing HTML to convert to entities.
     * @return a string of the converted HTML.
     */
    public static String convertToEntities(String htmlString) {
        String ltConverted = htmlString.replace("<", "&lt;");
        String gtConverted = ltConverted.replace(">", "&gt;");
        String converted = gtConverted.replace("\"","&quot;");
        // convert back the valid tags
        for(String validTag : Constants.VALID_DISCUSSION_TAGS) {
            converted = converted.replace("&lt;"+validTag+"&gt;",
                    "<"+validTag+">");
            converted = converted.replace("&lt;/"+validTag+"&gt;",
                    "</"+validTag+">");
        }
        return converted;
    }


    /**
     * Closes tags as necessary if user doesn't specify closing HTML tags.
     * Example: <i><u><b>hello</b> gets converted to <i><u><b>hello</b></u></i>
     *
     * Note: Doesn't necessarily produce valid HTML, but closing the tags
     * prevents the tag styling from bleeding into other sections of the HTML.
     *
     * @param htmlString String of HTML and text to add closing tags to.
     * @return String with tags closed.
     */
    public static String closeAllTags(String htmlString) {
        // create a regex to match all valid discussion HTML tags
        String validTagsRegex = "</?(";
        for(int i=0; i<Constants.VALID_DISCUSSION_TAGS.size(); i++) {
            if(i > 0) {
                validTagsRegex += "|";
            }
            validTagsRegex += Constants.VALID_DISCUSSION_TAGS.get(i);
        }
        validTagsRegex += ")>";
        Pattern validTagsPattern = Pattern.compile(validTagsRegex);
        Matcher validTagsMatcher = validTagsPattern.matcher(htmlString);
        // keep track of the HTML tags that have been opened but not closed yet
        Stack<String> openedTags = new Stack<String>();
        while (validTagsMatcher.find()) {
            String match = validTagsMatcher.group();
            for(String tag : Constants.VALID_DISCUSSION_TAGS) {
                if(("<"+tag+">").equals(match)){
                    // found an open tag
                    openedTags.add(tag);
                    break;
                }
                else if(("</"+tag+">").equals(match)) {
                    if(!openedTags.empty() && openedTags.peek().equals(tag)) {
                        // tag was closed properly
                        openedTags.pop();
                        break;
                    }
                }
            }
        }
        // add unclosed tags to end of HTML string
        while(!openedTags.empty()) {
            htmlString += ("</"+openedTags.pop()+">");
        }
        return htmlString;
    }
}