



<!DOCTYPE html>
<html lang="en" class="">
  <head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# object: http://ogp.me/ns/object# article: http://ogp.me/ns/article# profile: http://ogp.me/ns/profile#">
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="Content-Language" content="en">
    
    
    <title>hazelcast-code-samples/README.md at jpa-code-samples · tugrulaltun/hazelcast-code-samples</title>
    <link rel="search" type="application/opensearchdescription+xml" href="/opensearch.xml" title="GitHub">
    <link rel="fluid-icon" href="https://github.com/fluidicon.png" title="GitHub">
    <link rel="apple-touch-icon" sizes="57x57" href="/apple-touch-icon-114.png">
    <link rel="apple-touch-icon" sizes="114x114" href="/apple-touch-icon-114.png">
    <link rel="apple-touch-icon" sizes="72x72" href="/apple-touch-icon-144.png">
    <link rel="apple-touch-icon" sizes="144x144" href="/apple-touch-icon-144.png">
    <meta property="fb:app_id" content="1401488693436528">

      <meta content="@github" name="twitter:site" /><meta content="summary" name="twitter:card" /><meta content="tugrulaltun/hazelcast-code-samples" name="twitter:title" /><meta content="hazelcast-code-samples - Hazelcast Code Samples" name="twitter:description" /><meta content="https://avatars1.githubusercontent.com/u/1101682?v=3&amp;s=400" name="twitter:image:src" />
<meta content="GitHub" property="og:site_name" /><meta content="object" property="og:type" /><meta content="https://avatars1.githubusercontent.com/u/1101682?v=3&amp;s=400" property="og:image" /><meta content="tugrulaltun/hazelcast-code-samples" property="og:title" /><meta content="https://github.com/tugrulaltun/hazelcast-code-samples" property="og:url" /><meta content="hazelcast-code-samples - Hazelcast Code Samples" property="og:description" />

      <meta name="browser-stats-url" content="/_stats">
    <link rel="assets" href="https://assets-cdn.github.com/">
    <link rel="conduit-xhr" href="https://ghconduit.com:25035">
    <link rel="xhr-socket" href="/_sockets">
    <meta name="pjax-timeout" content="1000">
    <link rel="sudo-modal" href="/sessions/sudo_modal">

    <meta name="msapplication-TileImage" content="/windows-tile.png">
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="selected-link" value="repo_source" data-pjax-transient>
      <meta name="google-analytics" content="UA-3769691-2">

    <meta content="collector.githubapp.com" name="octolytics-host" /><meta content="collector-cdn.github.com" name="octolytics-script-host" /><meta content="github" name="octolytics-app-id" /><meta content="4EBAB836:2028:338B9B0:54CB8AA9" name="octolytics-dimension-request_id" /><meta content="1101682" name="octolytics-actor-id" /><meta content="tugrulaltun" name="octolytics-actor-login" /><meta content="d5b9ca5ca2d20e95d1e53692d8b1f94055bc66732c27f69a5fdd34aa63273c07" name="octolytics-actor-hash" />
    
    <meta content="Rails, view, blob#show" name="analytics-event" />

    
    
    <link rel="icon" type="image/x-icon" href="https://assets-cdn.github.com/favicon.ico">


    <meta content="authenticity_token" name="csrf-param" />
<meta content="b1zYMuAv/b38ncVyuHesWsAPkcG1Dy7YsZZau1bVomwN5QAf4TWe24JLpqblSHKk1WIKpurPGhiEi1RUcus4MA==" name="csrf-token" />

    <link href="https://assets-cdn.github.com/assets/github-f19e43be00c28904df28a1fd1fa3c117e5d2358dd3cf0f4216536f8737c2e033.css" media="all" rel="stylesheet" type="text/css" />
    <link href="https://assets-cdn.github.com/assets/github2-431ab37911886d4a1b2273b6b186fa167bf7ca9feb42b03c7e5fa9727ffbd8c6.css" media="all" rel="stylesheet" type="text/css" />
    
    


    <meta http-equiv="x-pjax-version" content="86f140ed686e9d51be22a1d285bb5a74">

      
  <meta name="description" content="hazelcast-code-samples - Hazelcast Code Samples">
  <meta name="go-import" content="github.com/tugrulaltun/hazelcast-code-samples git https://github.com/tugrulaltun/hazelcast-code-samples.git">

  <meta content="1101682" name="octolytics-dimension-user_id" /><meta content="tugrulaltun" name="octolytics-dimension-user_login" /><meta content="30023172" name="octolytics-dimension-repository_id" /><meta content="tugrulaltun/hazelcast-code-samples" name="octolytics-dimension-repository_nwo" /><meta content="true" name="octolytics-dimension-repository_public" /><meta content="true" name="octolytics-dimension-repository_is_fork" /><meta content="11708465" name="octolytics-dimension-repository_parent_id" /><meta content="hazelcast/hazelcast-code-samples" name="octolytics-dimension-repository_parent_nwo" /><meta content="11708465" name="octolytics-dimension-repository_network_root_id" /><meta content="hazelcast/hazelcast-code-samples" name="octolytics-dimension-repository_network_root_nwo" />
  <link href="https://github.com/tugrulaltun/hazelcast-code-samples/commits/jpa-code-samples.atom" rel="alternate" title="Recent Commits to hazelcast-code-samples:jpa-code-samples" type="application/atom+xml">

  </head>


  <body class="logged_in  env-production linux vis-public fork page-blob">
    <a href="#start-of-content" tabindex="1" class="accessibility-aid js-skip-to-content">Skip to content</a>
    <div class="wrapper">
      
      
      
      


      <div class="header header-logged-in true" role="banner">
  <div class="container clearfix">

    <a class="header-logo-invertocat" href="https://github.com/" data-hotkey="g d" aria-label="Homepage" ga-data-click="Header, go to dashboard, icon:logo">
  <span class="mega-octicon octicon-mark-github"></span>
</a>


      <div class="site-search repo-scope js-site-search" role="search">
          <form accept-charset="UTF-8" action="/tugrulaltun/hazelcast-code-samples/search" class="js-site-search-form" data-global-search-url="/search" data-repo-search-url="/tugrulaltun/hazelcast-code-samples/search" method="get"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /></div>
  <input type="text"
    class="js-site-search-field is-clearable"
    data-hotkey="s"
    name="q"
    placeholder="Search"
    data-global-scope-placeholder="Search GitHub"
    data-repo-scope-placeholder="Search"
    tabindex="1"
    autocapitalize="off">
  <div class="scope-badge">This repository</div>
</form>
      </div>
      <ul class="header-nav left" role="navigation">
        <li class="header-nav-item explore">
          <a class="header-nav-link" href="/explore" data-ga-click="Header, go to explore, text:explore">Explore</a>
        </li>
          <li class="header-nav-item">
            <a class="header-nav-link" href="https://gist.github.com" data-ga-click="Header, go to gist, text:gist">Gist</a>
          </li>
          <li class="header-nav-item">
            <a class="header-nav-link" href="/blog" data-ga-click="Header, go to blog, text:blog">Blog</a>
          </li>
        <li class="header-nav-item">
          <a class="header-nav-link" href="https://help.github.com" data-ga-click="Header, go to help, text:help">Help</a>
        </li>
      </ul>

    
<ul class="header-nav user-nav right" id="user-links">
  <li class="header-nav-item dropdown js-menu-container">
    <a class="header-nav-link name" href="/tugrulaltun" data-ga-click="Header, go to profile, text:username">
      <img alt="tuğrul" class="avatar" data-user="1101682" height="20" src="https://avatars3.githubusercontent.com/u/1101682?v=3&amp;s=40" width="20" />
      <span class="css-truncate">
        <span class="css-truncate-target">tugrulaltun</span>
      </span>
    </a>
  </li>

  <li class="header-nav-item dropdown js-menu-container">
    <a class="header-nav-link js-menu-target tooltipped tooltipped-s" href="#" aria-label="Create new..." data-ga-click="Header, create new, icon:add">
      <span class="octicon octicon-plus"></span>
      <span class="dropdown-caret"></span>
    </a>

    <div class="dropdown-menu-content js-menu-content">
      
<ul class="dropdown-menu">
  <li>
    <a href="/new" data-ga-click="Header, create new repository, icon:repo"><span class="octicon octicon-repo"></span> New repository</a>
  </li>
  <li>
    <a href="/organizations/new" data-ga-click="Header, create new organization, icon:organization"><span class="octicon octicon-organization"></span> New organization</a>
  </li>


    <li class="dropdown-divider"></li>
    <li class="dropdown-header">
      <span title="tugrulaltun/hazelcast-code-samples">This repository</span>
    </li>
      <li>
        <a href="/tugrulaltun/hazelcast-code-samples/settings/collaboration" data-ga-click="Header, create new collaborator, icon:person"><span class="octicon octicon-person"></span> New collaborator</a>
      </li>
</ul>

    </div>
  </li>

  <li class="header-nav-item">
        <a href="/notifications" aria-label="You have no unread notifications" class="header-nav-link notification-indicator tooltipped tooltipped-s" data-ga-click="Header, go to notifications, icon:read" data-hotkey="g n">
        <span class="mail-status all-read"></span>
        <span class="octicon octicon-inbox"></span>
</a>
  </li>

  <li class="header-nav-item">
    <a class="header-nav-link tooltipped tooltipped-s" href="/settings/profile" id="account_settings" aria-label="Settings" data-ga-click="Header, go to settings, icon:settings">
      <span class="octicon octicon-gear"></span>
    </a>
  </li>

  <li class="header-nav-item">
    <form accept-charset="UTF-8" action="/logout" class="logout-form" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="Y9NZ4h6SurxhzPQQHedM9iQb1vwIC8x6as6KhLskUHUMYNIaj/MbDad+wVL7hJkp57m1zbeESLfJ/RZNnzSwTA==" /></div>
      <button class="header-nav-link sign-out-button tooltipped tooltipped-s" aria-label="Sign out" data-ga-click="Header, sign out, icon:logout">
        <span class="octicon octicon-sign-out"></span>
      </button>
</form>  </li>

</ul>


    
  </div>
</div>

      

        


      <div id="start-of-content" class="accessibility-aid"></div>
          <div class="site" itemscope itemtype="http://schema.org/WebPage">
    <div id="js-flash-container">
      
    </div>
    <div class="pagehead repohead instapaper_ignore readability-menu">
      <div class="container">
        
<ul class="pagehead-actions">

    <li class="subscription">
      <form accept-charset="UTF-8" action="/notifications/subscribe" class="js-social-container" data-autosubmit="true" data-remote="true" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="PjNyDw+DRS/snmSYoZVSMATFsqZVmW5FA+XX7I2K5qHK3uWTPgxRKwYDc++H9TaZDakJeOPG7zEz2mhclIIaVQ==" /></div>  <input id="repository_id" name="repository_id" type="hidden" value="30023172" />

    <div class="select-menu js-menu-container js-select-menu">
      <a class="social-count js-social-count" href="/tugrulaltun/hazelcast-code-samples/watchers">
        1
      </a>
      <a href="/tugrulaltun/hazelcast-code-samples/subscription"
        class="minibutton select-menu-button with-count js-menu-target" role="button" tabindex="0" aria-haspopup="true">
        <span class="js-select-button">
          <span class="octicon octicon-eye"></span>
          Unwatch
        </span>
      </a>

      <div class="select-menu-modal-holder">
        <div class="select-menu-modal subscription-menu-modal js-menu-content" aria-hidden="true">
          <div class="select-menu-header">
            <span class="select-menu-title">Notifications</span>
            <span class="octicon octicon-x js-menu-close" role="button" aria-label="Close"></span>
          </div> <!-- /.select-menu-header -->

          <div class="select-menu-list js-navigation-container" role="menu">

            <div class="select-menu-item js-navigation-item " role="menuitem" tabindex="0">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <div class="select-menu-item-text">
                <input id="do_included" name="do" type="radio" value="included" />
                <h4>Not watching</h4>
                <span class="description">Be notified when participating or @mentioned.</span>
                <span class="js-select-button-text hidden-select-button-text">
                  <span class="octicon octicon-eye"></span>
                  Watch
                </span>
              </div>
            </div> <!-- /.select-menu-item -->

            <div class="select-menu-item js-navigation-item selected" role="menuitem" tabindex="0">
              <span class="select-menu-item-icon octicon octicon octicon-check"></span>
              <div class="select-menu-item-text">
                <input checked="checked" id="do_subscribed" name="do" type="radio" value="subscribed" />
                <h4>Watching</h4>
                <span class="description">Be notified of all conversations.</span>
                <span class="js-select-button-text hidden-select-button-text">
                  <span class="octicon octicon-eye"></span>
                  Unwatch
                </span>
              </div>
            </div> <!-- /.select-menu-item -->

            <div class="select-menu-item js-navigation-item " role="menuitem" tabindex="0">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <div class="select-menu-item-text">
                <input id="do_ignore" name="do" type="radio" value="ignore" />
                <h4>Ignoring</h4>
                <span class="description">Never be notified.</span>
                <span class="js-select-button-text hidden-select-button-text">
                  <span class="octicon octicon-mute"></span>
                  Stop ignoring
                </span>
              </div>
            </div> <!-- /.select-menu-item -->

          </div> <!-- /.select-menu-list -->

        </div> <!-- /.select-menu-modal -->
      </div> <!-- /.select-menu-modal-holder -->
    </div> <!-- /.select-menu -->

</form>
    </li>

  <li>
    
  <div class="js-toggler-container js-social-container starring-container ">

    <form accept-charset="UTF-8" action="/tugrulaltun/hazelcast-code-samples/unstar" class="js-toggler-form starred js-unstar-button" data-remote="true" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="K5WvJebmjDvqYM9i0NduhNqASvYX90bmkx54STsIcqNk5gNZtw43FN40h16FJkZRvR/b+F6ZnHbYJkXn2XuU4w==" /></div>
      <button
        class="minibutton with-count js-toggler-target star-button"
        aria-label="Unstar this repository" title="Unstar tugrulaltun/hazelcast-code-samples">
        <span class="octicon octicon-star"></span>
        Unstar
      </button>
        <a class="social-count js-social-count" href="/tugrulaltun/hazelcast-code-samples/stargazers">
          0
        </a>
</form>
    <form accept-charset="UTF-8" action="/tugrulaltun/hazelcast-code-samples/star" class="js-toggler-form unstarred js-star-button" data-remote="true" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="5GnzN9O5aJUz3I8JSYN93MA7CinxDZ3wb+otPdqNEzuK6MbCA/gOcx0cv0vrAzHJqrEwViMilmERwgH4M6yGqw==" /></div>
      <button
        class="minibutton with-count js-toggler-target star-button"
        aria-label="Star this repository" title="Star tugrulaltun/hazelcast-code-samples">
        <span class="octicon octicon-star"></span>
        Star
      </button>
        <a class="social-count js-social-count" href="/tugrulaltun/hazelcast-code-samples/stargazers">
          0
        </a>
</form>  </div>

  </li>


        <li>
          <a href="/tugrulaltun/hazelcast-code-samples/fork" class="minibutton with-count js-toggler-target fork-button tooltipped-n" title="Fork your own copy of tugrulaltun/hazelcast-code-samples to your account" aria-label="Fork your own copy of tugrulaltun/hazelcast-code-samples to your account" rel="nofollow" data-method="post">
            <span class="octicon octicon-repo-forked"></span>
            Fork
          </a>
          <a href="/tugrulaltun/hazelcast-code-samples/network" class="social-count">49</a>
        </li>

</ul>

        <h1 itemscope itemtype="http://data-vocabulary.org/Breadcrumb" class="entry-title public">
          <span class="mega-octicon octicon-repo-forked"></span>
          <span class="author"><a href="/tugrulaltun" class="url fn" itemprop="url" rel="author"><span itemprop="title">tugrulaltun</span></a></span><!--
       --><span class="path-divider">/</span><!--
       --><strong><a href="/tugrulaltun/hazelcast-code-samples" class="js-current-repository" data-pjax="#js-repo-pjax-container">hazelcast-code-samples</a></strong>

          <span class="page-context-loader">
            <img alt="" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
          </span>

            <span class="fork-flag">
              <span class="text">forked from <a href="/hazelcast/hazelcast-code-samples">hazelcast/hazelcast-code-samples</a></span>
            </span>
        </h1>
      </div><!-- /.container -->
    </div><!-- /.repohead -->

    <div class="container">
      <div class="repository-with-sidebar repo-container new-discussion-timeline  ">
        <div class="repository-sidebar clearfix">
            
<nav class="sunken-menu repo-nav js-repo-nav js-sidenav-container-pjax js-octicon-loaders"
     role="navigation"
     data-pjax="#js-repo-pjax-container"
     data-issue-count-url="/tugrulaltun/hazelcast-code-samples/issues/counts">
  <ul class="sunken-menu-group">
    <li class="tooltipped tooltipped-w" aria-label="Code">
      <a href="/tugrulaltun/hazelcast-code-samples/tree/jpa-code-samples" aria-label="Code" class="selected js-selected-navigation-item sunken-menu-item" data-hotkey="g c" data-selected-links="repo_source repo_downloads repo_commits repo_releases repo_tags repo_branches /tugrulaltun/hazelcast-code-samples/tree/jpa-code-samples">
        <span class="octicon octicon-code"></span> <span class="full-word">Code</span>
        <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>


    <li class="tooltipped tooltipped-w" aria-label="Pull Requests">
      <a href="/tugrulaltun/hazelcast-code-samples/pulls" aria-label="Pull Requests" class="js-selected-navigation-item sunken-menu-item" data-hotkey="g p" data-selected-links="repo_pulls /tugrulaltun/hazelcast-code-samples/pulls">
          <span class="octicon octicon-git-pull-request"></span> <span class="full-word">Pull Requests</span>
          <span class="js-pull-replace-counter"></span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>


  </ul>
  <div class="sunken-menu-separator"></div>
  <ul class="sunken-menu-group">

    <li class="tooltipped tooltipped-w" aria-label="Pulse">
      <a href="/tugrulaltun/hazelcast-code-samples/pulse" aria-label="Pulse" class="js-selected-navigation-item sunken-menu-item" data-selected-links="pulse /tugrulaltun/hazelcast-code-samples/pulse">
        <span class="octicon octicon-pulse"></span> <span class="full-word">Pulse</span>
        <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>

    <li class="tooltipped tooltipped-w" aria-label="Graphs">
      <a href="/tugrulaltun/hazelcast-code-samples/graphs" aria-label="Graphs" class="js-selected-navigation-item sunken-menu-item" data-selected-links="repo_graphs repo_contributors /tugrulaltun/hazelcast-code-samples/graphs">
        <span class="octicon octicon-graph"></span> <span class="full-word">Graphs</span>
        <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>
  </ul>


    <div class="sunken-menu-separator"></div>
    <ul class="sunken-menu-group">
      <li class="tooltipped tooltipped-w" aria-label="Settings">
        <a href="/tugrulaltun/hazelcast-code-samples/settings" aria-label="Settings" class="js-selected-navigation-item sunken-menu-item" data-selected-links="repo_settings /tugrulaltun/hazelcast-code-samples/settings">
          <span class="octicon octicon-tools"></span> <span class="full-word">Settings</span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>
    </ul>
</nav>

              <div class="only-with-full-nav">
                
  
<div class="clone-url open"
  data-protocol-type="http"
  data-url="/users/set_protocol?protocol_selector=http&amp;protocol_type=clone">
  <h3><span class="text-emphasized">HTTPS</span> clone URL</h3>
  <div class="input-group js-zeroclipboard-container">
    <input type="text" class="input-mini input-monospace js-url-field js-zeroclipboard-target"
           value="https://github.com/tugrulaltun/hazelcast-code-samples.git" readonly="readonly">
    <span class="input-group-button">
      <button aria-label="Copy to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>

  
<div class="clone-url "
  data-protocol-type="ssh"
  data-url="/users/set_protocol?protocol_selector=ssh&amp;protocol_type=clone">
  <h3><span class="text-emphasized">SSH</span> clone URL</h3>
  <div class="input-group js-zeroclipboard-container">
    <input type="text" class="input-mini input-monospace js-url-field js-zeroclipboard-target"
           value="git@github.com:tugrulaltun/hazelcast-code-samples.git" readonly="readonly">
    <span class="input-group-button">
      <button aria-label="Copy to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>

  
<div class="clone-url "
  data-protocol-type="subversion"
  data-url="/users/set_protocol?protocol_selector=subversion&amp;protocol_type=clone">
  <h3><span class="text-emphasized">Subversion</span> checkout URL</h3>
  <div class="input-group js-zeroclipboard-container">
    <input type="text" class="input-mini input-monospace js-url-field js-zeroclipboard-target"
           value="https://github.com/tugrulaltun/hazelcast-code-samples" readonly="readonly">
    <span class="input-group-button">
      <button aria-label="Copy to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>



<p class="clone-options">You can clone with
  <a href="#" class="js-clone-selector" data-protocol="http">HTTPS</a>, <a href="#" class="js-clone-selector" data-protocol="ssh">SSH</a>, or <a href="#" class="js-clone-selector" data-protocol="subversion">Subversion</a>.
  <a href="https://help.github.com/articles/which-remote-url-should-i-use" class="help tooltipped tooltipped-n" aria-label="Get help on which URL is right for you.">
    <span class="octicon octicon-question"></span>
  </a>
</p>



                <a href="/tugrulaltun/hazelcast-code-samples/archive/jpa-code-samples.zip"
                   class="minibutton sidebar-button"
                   aria-label="Download the contents of tugrulaltun/hazelcast-code-samples as a zip file"
                   title="Download the contents of tugrulaltun/hazelcast-code-samples as a zip file"
                   rel="nofollow">
                  <span class="octicon octicon-cloud-download"></span>
                  Download ZIP
                </a>
              </div>
        </div><!-- /.repository-sidebar -->

        <div id="js-repo-pjax-container" class="repository-content context-loader-container" data-pjax-container>
          

<a href="/tugrulaltun/hazelcast-code-samples/blob/7854e5024d08e8862e939978ec3b97af5593a918/hazelcast-integration/hibernate-2ndlevel-cache/README.md" class="hidden js-permalink-shortcut" data-hotkey="y">Permalink</a>

<!-- blob contrib key: blob_contributors:v21:c68690fad0603aa2d6c9fbba187531ed -->

<div class="file-navigation js-zeroclipboard-container">
  
<div class="select-menu js-menu-container js-select-menu left">
  <span class="minibutton select-menu-button js-menu-target css-truncate" data-hotkey="w"
    data-master-branch="master"
    data-ref="jpa-code-samples"
    title="jpa-code-samples"
    role="button" aria-label="Switch branches or tags" tabindex="0" aria-haspopup="true">
    <span class="octicon octicon-git-branch"></span>
    <i>branch:</i>
    <span class="js-select-button css-truncate-target">jpa-code-sampl…</span>
  </span>

  <div class="select-menu-modal-holder js-menu-content js-navigation-container" data-pjax aria-hidden="true">

    <div class="select-menu-modal">
      <div class="select-menu-header">
        <span class="select-menu-title">Switch branches/tags</span>
        <span class="octicon octicon-x js-menu-close" role="button" aria-label="Close"></span>
      </div> <!-- /.select-menu-header -->

      <div class="select-menu-filters">
        <div class="select-menu-text-filter">
          <input type="text" aria-label="Find or create a branch…" id="context-commitish-filter-field" class="js-filterable-field js-navigation-enable" placeholder="Find or create a branch…">
        </div>
        <div class="select-menu-tabs">
          <ul>
            <li class="select-menu-tab">
              <a href="#" data-tab-filter="branches" class="js-select-menu-tab">Branches</a>
            </li>
            <li class="select-menu-tab">
              <a href="#" data-tab-filter="tags" class="js-select-menu-tab">Tags</a>
            </li>
          </ul>
        </div><!-- /.select-menu-tabs -->
      </div><!-- /.select-menu-filters -->

      <div class="select-menu-list select-menu-tab-bucket js-select-menu-tab-bucket" data-tab-filter="branches">

        <div data-filterable-for="context-commitish-filter-field" data-filterable-type="substring">


            <div class="select-menu-item js-navigation-item selected">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/blob/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="jpa-code-samples"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="jpa-code-samples">jpa-code-samples</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/blob/maintenance-3.x/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="maintenance-3.x"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="maintenance-3.x">maintenance-3.x</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/blob/master/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="master"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="master">master</a>
            </div> <!-- /.select-menu-item -->
        </div>

          <form accept-charset="UTF-8" action="/tugrulaltun/hazelcast-code-samples/branches" class="js-create-branch select-menu-item select-menu-new-item-form js-navigation-item js-new-item-form" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="G6iIIgacG9w0uwQCsaMOKtDykmxpNloXg+fcUTQpLNbo3vDkyvR+r6BTn+ti7+WVgfmJKmVEtJFnwhsdjOEbKQ==" /></div>
            <span class="octicon octicon-git-branch select-menu-item-icon"></span>
            <div class="select-menu-item-text">
              <h4>Create branch: <span class="js-new-item-name"></span></h4>
              <span class="description">from ‘jpa-code-samples’</span>
            </div>
            <input type="hidden" name="name" id="name" class="js-new-item-value">
            <input type="hidden" name="branch" id="branch" value="jpa-code-samples">
            <input type="hidden" name="path" id="path" value="hazelcast-integration/hibernate-2ndlevel-cache/README.md">
          </form> <!-- /.select-menu-item -->

      </div> <!-- /.select-menu-list -->

      <div class="select-menu-list select-menu-tab-bucket js-select-menu-tab-bucket" data-tab-filter="tags">
        <div data-filterable-for="context-commitish-filter-field" data-filterable-type="substring">


            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.4-EA/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.4-EA"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.4-EA">v3.4-EA</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.4/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.4"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.4">v3.4</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3-RC3/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3-RC3"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3-RC3">v3.3-RC3</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3-RC2/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3-RC2"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3-RC2">v3.3-RC2</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3-RC1/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3-RC1"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3-RC1">v3.3-RC1</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3-EA2/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3-EA2"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3-EA2">v3.3-EA2</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3.5/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3.5"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3.5">v3.3.5</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3.4/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3.4"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3.4">v3.3.4</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3.3/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3.3"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3.3">v3.3.3</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3.2/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3.2"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3.2">v3.3.2</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3.1/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3.1"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3.1">v3.3.1</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.3/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.3"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.3">v3.3</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.2/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.2"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.2">v3.2</a>
            </div> <!-- /.select-menu-item -->
            <div class="select-menu-item js-navigation-item ">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/tugrulaltun/hazelcast-code-samples/tree/v3.1/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 data-name="v3.1"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text css-truncate-target"
                 title="v3.1">v3.1</a>
            </div> <!-- /.select-menu-item -->
        </div>

        <div class="select-menu-no-results">Nothing to show</div>
      </div> <!-- /.select-menu-list -->

    </div> <!-- /.select-menu-modal -->
  </div> <!-- /.select-menu-modal-holder -->
</div> <!-- /.select-menu -->

  <div class="button-group right">
    <a href="/tugrulaltun/hazelcast-code-samples/find/jpa-code-samples"
          class="js-show-file-finder minibutton empty-icon tooltipped tooltipped-s"
          data-pjax
          data-hotkey="t"
          aria-label="Quickly jump between files">
      <span class="octicon octicon-list-unordered"></span>
    </a>
    <button aria-label="Copy file path to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
  </div>

  <div class="breadcrumb js-zeroclipboard-target">
    <span class='repo-root js-repo-root'><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/tugrulaltun/hazelcast-code-samples/tree/jpa-code-samples" class="" data-branch="jpa-code-samples" data-direction="back" data-pjax="true" itemscope="url"><span itemprop="title">hazelcast-code-samples</span></a></span></span><span class="separator">/</span><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/tugrulaltun/hazelcast-code-samples/tree/jpa-code-samples/hazelcast-integration" class="" data-branch="jpa-code-samples" data-direction="back" data-pjax="true" itemscope="url"><span itemprop="title">hazelcast-integration</span></a></span><span class="separator">/</span><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/tugrulaltun/hazelcast-code-samples/tree/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache" class="" data-branch="jpa-code-samples" data-direction="back" data-pjax="true" itemscope="url"><span itemprop="title">hibernate-2ndlevel-cache</span></a></span><span class="separator">/</span><strong class="final-path">README.md</strong>
  </div>
</div>

<include-fragment class="commit commit-loader file-history-tease" src="/tugrulaltun/hazelcast-code-samples/contributors/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md">
  <div class="file-history-tease-header">
    Fetching contributors&hellip;
  </div>

  <div class="participation">
    <p class="loader-loading"><img alt="" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32-EAF2F5.gif" width="16" /></p>
    <p class="loader-error">Cannot retrieve contributors at this time</p>
  </div>
</include-fragment>
<div class="file-box">
  <div class="file">
    <div class="meta clearfix">
      <div class="info file-name">
          <span>49 lines (44 sloc)</span>
          <span class="meta-divider"></span>
        <span>1.724 kb</span>
      </div>
      <div class="actions">
        <div class="button-group">
          <a href="/tugrulaltun/hazelcast-code-samples/raw/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md" class="minibutton " id="raw-url">Raw</a>
            <a href="/tugrulaltun/hazelcast-code-samples/blame/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md" class="minibutton js-update-url-with-hash">Blame</a>
          <a href="/tugrulaltun/hazelcast-code-samples/commits/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md" class="minibutton " rel="nofollow">History</a>
        </div><!-- /.button-group -->


              <a class="octicon-button js-update-url-with-hash"
                 href="/tugrulaltun/hazelcast-code-samples/edit/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
                 aria-label="Edit this file"
                 data-method="post" rel="nofollow" data-hotkey="e"><span class="octicon octicon-pencil"></span></a>

            <a class="octicon-button danger"
               href="/tugrulaltun/hazelcast-code-samples/delete/jpa-code-samples/hazelcast-integration/hibernate-2ndlevel-cache/README.md"
               aria-label="Delete this file"
               data-method="post" data-test-id="delete-blob-file" rel="nofollow">
          <span class="octicon octicon-trashcan"></span>
        </a>
      </div><!-- /.actions -->
    </div>
    
  <div id="readme" class="blob instapaper_body">
    <article class="markdown-body entry-content" itemprop="mainContentOfPage"><h1>
<a id="user-content-hibernate-2nd-level-cache-with-hazelcast" class="anchor" href="#hibernate-2nd-level-cache-with-hazelcast" aria-hidden="true"><span class="octicon octicon-link"></span></a>Hibernate 2nd Level Cache with Hazelcast by using JPA</h1>

<p>In this repository, you can find a sample implementation of hibernate 2nd level cache with hazelcast by using JPA. You can also find detailed explanation at <a href="http://hazelcast.org/">http://hazelcast.org/</a> </p>

<h2>
<a id="user-content-prerequisites" class="anchor" href="#prerequisites" aria-hidden="true"><span class="octicon octicon-link"></span></a>Prerequisites</h2>

<p>You should have installed Apache Maven(<a href="http://maven.apache.org/download.cgi">http://maven.apache.org/download.cgi</a>).</p>

<p>By default some dependencies added to project in "pom.xml" file as follows:</p>

<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;com.hazelcast&lt;/groupId&gt;
    &lt;artifactId&gt;hazelcast-hibernate3&lt;/artifactId&gt;
    &lt;version&gt;3.0-RC2&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;com.hazelcast&lt;/groupId&gt;
    &lt;artifactId&gt;hazelcast&lt;/artifactId&gt;
    &lt;version&gt;${hazelcast.version}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;javassist&lt;/groupId&gt;
    &lt;artifactId&gt;javassist&lt;/artifactId&gt;
    &lt;version&gt;3.12.1.GA&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;org.apache.derby&lt;/groupId&gt;
    &lt;artifactId&gt;jderby&lt;/artifactId&gt;
    &lt;version&gt;10.10.2.0&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;org.hibernate&lt;/groupId&gt;
    &lt;artifactId&gt;hibernate-entitymanager&lt;/artifactId&gt;
    &lt;version&gt;3.6.9.Final&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;org.hibernate.javax.persistence&lt;/groupId&gt;
    &lt;artifactId&gt;hibernate-jpa-2.0-api&lt;/artifactId&gt;
    &lt;version&gt;1.0.0.Final&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<p>But project is also compatible with hibernate 3.X.X versions. You can change these entries accordingly.</p>

<h2>
<a id="user-content-how-to-run-sample-application" class="anchor" href="#how-to-run-sample-application" aria-hidden="true"><span class="octicon octicon-link"></span></a>How to Run Sample Application</h2>

<p>1) Compile project using:</p>

<pre><code>mvn -X compile
</code></pre>

<p>2) Create database using:</p>

<pre><code>mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.CreateDB"
</code></pre>

<p>3) After running the following code, you can add or delete employees. Start with writing help in the application:</p>

<pre><code>mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.ManageEmployeeJPA"
</code></pre>

</article>
  </div>

  </div>
</div>

<a href="#jump-to-line" rel="facebox[.linejump]" data-hotkey="l" style="display:none">Jump to Line</a>
<div id="jump-to-line" style="display:none">
  <form accept-charset="UTF-8" class="js-jump-to-line-form">
    <input class="linejump-input js-jump-to-line-field" type="text" placeholder="Jump to line&hellip;" autofocus>
    <button type="submit" class="button">Go</button>
  </form>
</div>

        </div>

      </div><!-- /.repo-container -->
      <div class="modal-backdrop"></div>
    </div><!-- /.container -->
  </div><!-- /.site -->


    </div><!-- /.wrapper -->

      <div class="container">
  <div class="site-footer" role="contentinfo">
    <ul class="site-footer-links right">
      <li><a href="https://status.github.com/">Status</a></li>
      <li><a href="https://developer.github.com">API</a></li>
      <li><a href="http://training.github.com">Training</a></li>
      <li><a href="http://shop.github.com">Shop</a></li>
      <li><a href="/blog">Blog</a></li>
      <li><a href="/about">About</a></li>

    </ul>

    <a href="/" aria-label="Homepage">
      <span class="mega-octicon octicon-mark-github" title="GitHub"></span>
    </a>

    <ul class="site-footer-links">
      <li>&copy; 2015 <span title="0.04900s from github-fe136-cp1-prd.iad.github.net">GitHub</span>, Inc.</li>
        <li><a href="/site/terms">Terms</a></li>
        <li><a href="/site/privacy">Privacy</a></li>
        <li><a href="/security">Security</a></li>
        <li><a href="/contact">Contact</a></li>
    </ul>
  </div><!-- /.site-footer -->
</div><!-- /.container -->


    <div class="fullscreen-overlay js-fullscreen-overlay" id="fullscreen_overlay">
  <div class="fullscreen-container js-suggester-container">
    <div class="textarea-wrap">
      <textarea name="fullscreen-contents" id="fullscreen-contents" class="fullscreen-contents js-fullscreen-contents" placeholder=""></textarea>
      <div class="suggester-container">
        <div class="suggester fullscreen-suggester js-suggester js-navigation-container"></div>
      </div>
    </div>
  </div>
  <div class="fullscreen-sidebar">
    <a href="#" class="exit-fullscreen js-exit-fullscreen tooltipped tooltipped-w" aria-label="Exit Zen Mode">
      <span class="mega-octicon octicon-screen-normal"></span>
    </a>
    <a href="#" class="theme-switcher js-theme-switcher tooltipped tooltipped-w"
      aria-label="Switch themes">
      <span class="octicon octicon-color-mode"></span>
    </a>
  </div>
</div>



    <div id="ajax-error-message" class="flash flash-error">
      <span class="octicon octicon-alert"></span>
      <a href="#" class="octicon octicon-x flash-close js-ajax-error-dismiss" aria-label="Dismiss error"></a>
      Something went wrong with that request. Please try again.
    </div>


      <script crossorigin="anonymous" src="https://assets-cdn.github.com/assets/frameworks-af95b05cb14b7a29b0457c26b4a1d24151f4a47842c8e74bd556622f347b9d3d.js" type="text/javascript"></script>
      <script async="async" crossorigin="anonymous" src="https://assets-cdn.github.com/assets/github-c2208b3421cde4b6965bb7ac184dc5ac3bcc3baf86f8daa0aeedf593426d01d7.js" type="text/javascript"></script>
      
      
  </body>
</html>

