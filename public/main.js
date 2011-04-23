/*
File: main.js
Name: Peter Graham
Class: CS 461
Project 1
Date: April 22
*/

/* 
Initialize things to execute when DOM is ready to be manipulated.
*/
$(document).ready(function() {
    
    // add the default labels to input fields
    $(".noJsSpacer").removeClass("noJsSpacer");
    $(".default-label").inFieldLabels();
    jQuery.validator.setDefaults({
        messages: {required:""}
    });
    $(".validate-form").validate({
		rules: {
                    email:"required",
                    password:"required",
                    topicName:"required"
                }
            });
    
    // AJAX vote when you click the heart icon
    $(".likeit").click(onVoteClick);
});

/*
Prompts a user to login.
*/
function promptLogin() {
    alert("You must be logged in to perform this action.");
}

/*
Do a client-side check for if visitor is logged in.
Note: never entirely rely on this. Always do a server-side check.
@return true if visitor is logged in according to the client side.
*/
function isLoggedIn() {
    return $("#isloggedin").text() == "true";
}

/*
Checks if logged in and votes if so. Otherwise, prompts login.
*/
function onVoteClick(){
    if(isLoggedIn()) {
        var liContainer = $(this).parent().parent();
        voteOn(liContainer.attr("id"));
    }
    else {
        promptLogin();
    }
    return !1;
}

/* 
Vote up the update with the given update ID.
@param updateId is the ID of the element corresponding to the vote to update
*/
function voteOn(updateId) {
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
    //ajax vote submit
    $.ajax({url:'/vote/'+updateId.substr(1)+'?ajax=1'});
}
