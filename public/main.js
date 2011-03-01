/*
File: main.js
Name: Peter Graham
Class: CS 461
Project 1
Date: February 16
*/
/* Things to execute when DOM is ready to be manipulated. */
$(document).ready(function() {
    
    $(".noJsSpacer").removeClass("noJsSpacer");
    $(".default-label").inFieldLabels();
    
    /* Default value code from:
     * http://www.electrictoolbox.com/jquery-change-default-value-on-focus/
     */
    $('.default-value').each(function() {
        var default_value = this.value;
        $(this).focus(function() {
            if(this.value == default_value) {
                this.value = '';
            }
        });
        $(this).blur(function() {
            if(this.value == '') {
                this.value = default_value;
            }
        });
    });
    
    // get the current page type -- homeview (homepage) or subview (subpage)
    var pageType = $("body").attr("id");
    if(pageType == "homeview") {
        // execute homepage-specific code
        $(".likeit").click(function(){
            var liContainer = $(this).parent().parent();
            voteUp(liContainer.attr("id"));
            return !1;
        });
    }
    else {
        // execute subpage-specific code
    }
});


/* Vote up the update with the given update ID */
function voteUp(updateId) {
    // the container of the voting section
    var voteContainer = $("#"+updateId).children(".like");
    
    // currently displayed count and new value for count
    var curCount = $(voteContainer).children(".votecount");
    var newCount = parseInt($(curCount).text());
    
    // check if user has already voted on this update
    var voteButton = voteContainer.children(".likeit");
    if(voteButton.hasClass("clicked")) {
        // undo their vote
        newCount -= 1;
        voteButton.removeClass("clicked");
    }
    else {
        // add a vote
        newCount += 1;
        voteButton.addClass("clicked");
    }
    // set the new count text
    curCount.text(newCount);
    //TODO: insert some ajax here, check vote again on server-side
}
