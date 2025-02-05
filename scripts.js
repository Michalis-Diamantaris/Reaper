$(document).ready(function () {

  $(window).scroll(function() {
    console.log(window.scrollY);
    if(window.scrollY >76)
    {
      $('.banner-area').css('cssText','position:fixed; top:0; width:100%;');
      $('.banner-contents').addClass('stickyBanner');
      $('.paper-info').css('margin-top','172px');
    }
    else
    {
      $('.banner-area').css('cssText','position:static; top:unset; width:100%;');

      if($('.banner-contents').hasClass('stickyBanner'))
      {
        $('.banner-contents').removeClass('stickyBanner');
        $('.paper-info').css('margin-top','0');
      }

    }
  });

});
