/*
 * Overrides various default values of jQuery mobile framework.
 */
$(document).bind("mobileinit", function(){
  /*
   * Allow zooming for mobile user agents. Source:
   * http://forum.jquery.com/topic/problem-with-the-zoom-on-ipad
   */
  $.mobile.metaViewportContent = "width=device-width, minimum-scale=1, maximum-scale=2";
});