/* Blog Digest redesign, built from seo/theme/redesign.xml by build_loader.py. Do not edit by hand. */
(function () {
  var me = document.currentScript, box = me && me.closest('.widget');
  if (box) box.style.display = 'none';
  if (window.bdRedesign) return;
  window.bdRedesign = true;
  var f = document.createElement('link'); f.rel = 'stylesheet'; f.href = "https://fonts.googleapis.com/css2?family=Fraunces:ital,wght@0,700;0,800;1,700&display=swap";
  var s = document.createElement('style'); s.id = 'bd-redesign-v2'; s.textContent = "\n:root{\n  --bd-bg:#f7f4ee;--bd-surface:#ffffff;--bd-surface-2:#fbf9f5;--bd-ink:#15171d;--bd-muted:#5d6271;--bd-line:#e8e2d6;\n  --bd-accent:#e2462c;--bd-accent-ink:#ffffff;--bd-accent-soft:#fde8e3;--bd-gold:#f2a33a;--bd-deep:#12141b;\n  --bd-shadow:0 1px 2px rgba(20,22,29,.05),0 8px 24px rgba(20,22,29,.07);\n  --bd-shadow-lg:0 2px 4px rgba(20,22,29,.06),0 22px 48px rgba(20,22,29,.14);\n  --bd-radius:18px;--bd-serif:'Fraunces',Georgia,serif;--bd-sans:system-ui,-apple-system,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;\n  --bd-ease:cubic-bezier(.2,.8,.2,1);\n}\nhtml.bd-dark{\n  --bd-bg:#0e1015;--bd-surface:#161920;--bd-surface-2:#1b1f27;--bd-ink:#ecebe7;--bd-muted:#a4a8b3;--bd-line:#272b35;\n  --bd-accent:#ff6a4d;--bd-accent-soft:#3a1f1a;--bd-deep:#090a0e;\n  --bd-shadow:0 1px 2px rgba(0,0,0,.4),0 8px 24px rgba(0,0,0,.35);--bd-shadow-lg:0 22px 48px rgba(0,0,0,.55);\n}\nhtml{scroll-behavior:smooth}\nbody{background:var(--bd-bg)!important;color:var(--bd-ink);font-family:var(--bd-sans)!important;-webkit-font-smoothing:antialiased;transition:background .3s,color .3s}\n#outer-wrapper{background:transparent!important;max-width:none!important;box-shadow:none!important}\na{transition:color .2s}\n::selection{background:var(--bd-accent);color:#fff}\n:focus-visible{outline:2px solid var(--bd-accent);outline-offset:3px;border-radius:6px}\n\n/* Top bar and header */\n#top-bar{background:var(--bd-deep)!important;border:0!important}\n#top-bar,#top-bar a{color:#cfd2da!important;font-family:var(--bd-sans)!important;font-size:12.5px!important;letter-spacing:.02em}\n#top-bar a:hover{color:#fff!important}\n#header-wrap{position:sticky;top:0;z-index:999;background:color-mix(in srgb,var(--bd-surface) 82%,transparent)!important;\n  -webkit-backdrop-filter:saturate(180%) blur(14px);backdrop-filter:saturate(180%) blur(14px);border-bottom:1px solid var(--bd-line);transition:box-shadow .3s}\n#header-wrap.bd-scrolled{box-shadow:var(--bd-shadow)}\n#header-wrap .header-header{background:transparent!important;box-shadow:none!important}\n.header-header .container{display:flex!important;align-items:center;justify-content:space-between;gap:16px;border:0!important}\n.header-logo{float:none!important;width:auto!important;margin:0!important;padding:14px 0!important}\n.header-image-wrapper{display:flex!important;align-items:center;gap:12px;text-decoration:none!important}\n.header-image-wrapper img{display:none!important}\n.header-image-wrapper::before{content:'B';display:grid;place-items:center;width:42px;height:42px;border-radius:13px;flex:none;\n  background:linear-gradient(135deg,var(--bd-accent),var(--bd-gold));color:#fff;font:italic 800 25px/1 var(--bd-serif);\n  box-shadow:0 8px 20px rgba(226,70,44,.32),inset 0 -2px 0 rgba(0,0,0,.12)}\n.header-image-wrapper::after{content:'Blog Digest';font-family:var(--bd-serif);font-weight:800;font-size:25px;letter-spacing:-.02em;color:var(--bd-ink);white-space:nowrap}\n#main-menu ul>li>a,.mobile-menu ul>li>a{font-family:var(--bd-sans)!important;font-weight:600!important;font-size:14px!important;text-transform:none!important;\n  color:var(--bd-ink)!important;border-radius:999px;padding:0 14px!important;line-height:36px!important;height:auto!important;background:transparent!important}\n#main-menu ul>li>a:hover,#main-menu ul>li.bd-active>a{background:var(--bd-accent-soft)!important;color:var(--bd-accent)!important}\n#main-menu ul>li>a::before,#main-menu ul>li>a::after{display:none!important}\n.header-menu,#main-menu,#main-menu .widget{display:flex!important;align-items:center;float:none!important;background:transparent!important}\n.header-menu{width:auto!important;height:auto!important;position:relative}\n#main-menu-nav{display:flex!important;align-items:center;gap:4px}\n#main-menu-nav>li{float:none!important;height:auto!important}\n.bd-tools{display:flex;align-items:center;gap:8px;margin-left:10px}\n.bd-icon-btn{width:40px;height:40px;border-radius:50%;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink)!important;\n  display:inline-grid;place-items:center;cursor:pointer;padding:0;transition:transform .25s var(--bd-ease),border-color .2s,color .2s}\n.bd-icon-btn:hover{transform:rotate(-12deg) scale(1.06);border-color:var(--bd-accent);color:var(--bd-accent)!important}\n.bd-icon-btn svg{width:18px;height:18px}\n\n/* Removed integrations */\n#top-bar-social,#onesignal-slidedown-container,#onesignal-bell-container,#onesignal-popover-container,.onesignal-customlink-container,\n.post-share li.pinterest,#top-bar .social li.facebook,#top-bar .social li.twitter,#top-bar .social li.pinterest,.fb-comments,.facebook-tab{display:none!important}\n\n/* Search overlay */\n.bd-search-modal{position:fixed;inset:0;z-index:10001;display:flex;flex-direction:column;align-items:center;padding:14vh 16px 16px;background:rgba(10,11,15,.55);\n  -webkit-backdrop-filter:blur(6px);backdrop-filter:blur(6px);opacity:0;pointer-events:none;transition:opacity .25s}\n.bd-search-modal.bd-open{opacity:1;pointer-events:auto}\n.bd-search-modal form{width:min(640px,100%);display:flex;gap:8px;padding:8px;border-radius:20px;background:var(--bd-surface);box-shadow:var(--bd-shadow-lg);\n  border:1px solid var(--bd-line);transform:translateY(-12px) scale(.98);transition:transform .3s var(--bd-ease)}\n.bd-search-modal.bd-open form{transform:none}\n.bd-search-modal input{flex:1;min-width:0;border:0;outline:0;background:transparent;color:var(--bd-ink);font:500 18px var(--bd-sans);padding:0 14px}\n.bd-search-modal button{border:0;border-radius:14px;background:var(--bd-accent);color:#fff;font:700 15px var(--bd-sans);padding:14px 22px;cursor:pointer}\n.bd-search-modal p{width:min(640px,100%);margin:14px 0 0;color:#d9dbe2;font:500 13px var(--bd-sans);text-align:center}\n\n/* Ticker */\n#break-wrapper-outer{background:transparent!important;border:0!important}\n#break-wrapper{background:var(--bd-surface)!important;border:1px solid var(--bd-line)!important;border-radius:999px;margin-top:18px!important;\n  box-shadow:var(--bd-shadow);overflow:hidden;padding:0 8px 0 0!important;height:auto!important;min-height:44px}\n#break-section .widget{display:flex!important;align-items:center;min-height:44px}\n#break-section .widget-title{background:transparent!important;border:0!important;padding:0!important;margin:0!important;flex:none}\n#break-section .widget-title::before{display:none!important}\n#break-section .widget-title h3.title{background:linear-gradient(135deg,var(--bd-accent),var(--bd-gold))!important;border-radius:999px!important;color:#fff!important;\n  font:700 11.5px/26px var(--bd-sans)!important;letter-spacing:.08em;text-transform:uppercase!important;padding:0 14px!important;height:26px!important;margin:0 10px 0 9px!important}\n#break-section .widget-title h3.title::after,#break-section .widget-title h3.title::before{display:none!important}\n#break-section .widget-content{flex:1;min-width:0}\n#break-section .ticker-item{display:flex!important;align-items:center;height:44px}\n#break-section .ticker-item .post-image-link,#break-section .ticker-item .post-tag{display:none!important}\n#break-section .ticker-item .post-title{margin:0!important;height:auto!important;font:500 14.5px/44px var(--bd-sans)!important;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}\n#break-section a{color:var(--bd-ink)!important;font-family:var(--bd-sans)!important;font-weight:500!important}\n#break-section a:hover{color:var(--bd-accent)!important}\n#break-section .owl-nav{top:50%!important;transform:translateY(-50%);margin:0!important}\n#break-section .owl-nav>div,#break-section .owl-nav button{background:var(--bd-accent-soft)!important;color:var(--bd-accent)!important;border-radius:50%!important}\n\n/* Homepage hero */\n.bd-hero{position:relative;overflow:hidden;box-sizing:border-box;width:auto!important;max-width:1170px!important;margin:28px auto 8px!important;padding:64px 56px;\n  border-radius:32px;background:var(--bd-deep);color:#f4f2ee;isolation:isolate}\n.bd-hero *,.bd-topics *,.bd-closer *{box-sizing:border-box}\n.bd-hero::before,.bd-hero::after{content:'';position:absolute;z-index:-1;border-radius:50%;filter:blur(70px);opacity:.75;animation:bd-float 16s ease-in-out infinite alternate}\n.bd-hero::before{width:520px;height:520px;background:radial-gradient(circle,#ff5a36,transparent 65%);top:-200px;right:-80px}\n.bd-hero::after{width:460px;height:460px;background:radial-gradient(circle,#6b5cff,transparent 65%);bottom:-240px;left:-120px;animation-delay:-8s}\n.bd-hero-grain{position:absolute;inset:0;z-index:-1;opacity:.35;pointer-events:none;\n  background-image:radial-gradient(rgba(255,255,255,.07) 1px,transparent 1px);background-size:22px 22px;\n  -webkit-mask-image:linear-gradient(120deg,transparent 20%,#000 70%);mask-image:linear-gradient(120deg,transparent 20%,#000 70%)}\n@keyframes bd-float{0%{transform:translate(0,0) scale(1)}100%{transform:translate(-60px,40px) scale(1.15)}}\n.bd-hero-grid{display:grid;grid-template-columns:1.2fr 1fr;gap:52px;align-items:center}\n.bd-hero-grid>div{min-width:0}\n.bd-eyebrow{display:inline-flex;align-items:center;gap:8px;font:600 12.5px/1 var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;\n  color:#ffd2c7;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.14);padding:9px 14px;border-radius:999px}\n.bd-eyebrow i{width:8px;height:8px;border-radius:50%;background:#4ade80;box-shadow:0 0 0 4px rgba(74,222,128,.2);animation:bd-pulse 2s infinite}\n@keyframes bd-pulse{50%{box-shadow:0 0 0 8px rgba(74,222,128,0)}}\n.bd-hero h1{font-family:var(--bd-serif)!important;font-weight:800;font-size:clamp(38px,5.4vw,66px);line-height:1.02;letter-spacing:-.035em;margin:22px 0 18px;color:#fff}\n.bd-hero h1 em{font-style:italic;font-weight:700;background:linear-gradient(100deg,#ff8a65,#ffc46b 60%,#ff8a65);-webkit-background-clip:text;background-clip:text;color:transparent;\n  background-size:200% auto;animation:bd-shine 6s linear infinite;padding-right:.08em}\n@keyframes bd-shine{to{background-position:200% center}}\n.bd-hero p.bd-lead{font:400 18px/1.65 var(--bd-sans);color:#c9ccd6;max-width:540px;margin:0 0 28px}\n.bd-search{display:flex;gap:8px;max-width:520px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.16);border-radius:999px;padding:6px;\n  transition:border-color .2s,background .2s}\n.bd-search:focus-within{border-color:#ff8a65;background:rgba(255,255,255,.12)}\n.bd-search input{flex:1;min-width:0;background:transparent;border:0;outline:0;color:#fff;font:500 15.5px var(--bd-sans);padding:0 16px}\n.bd-search input::placeholder{color:#9aa0ad}\n.bd-search button{border:0;border-radius:999px;background:linear-gradient(135deg,var(--bd-accent),#ff8a3d);color:#fff;font:700 14.5px var(--bd-sans);padding:13px 22px;cursor:pointer;transition:transform .2s,box-shadow .2s}\n.bd-search button:hover{transform:translateY(-1px);box-shadow:0 10px 24px rgba(226,70,44,.45)}\n.bd-stats{display:flex;gap:30px;margin-top:30px;flex-wrap:wrap}\n.bd-stats b{display:block;font:800 28px/1 var(--bd-serif);color:#fff}\n.bd-stats span{font:500 13px var(--bd-sans);color:#9aa0ad}\n.bd-guides{position:relative;padding:22px;border-radius:26px;background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.12);\n  -webkit-backdrop-filter:blur(10px);backdrop-filter:blur(10px);box-shadow:0 30px 60px rgba(0,0,0,.35)}\n.bd-guides-head{display:flex;align-items:center;justify-content:space-between;margin:0 4px 14px;font:700 12px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:#ffb199}\n.bd-guides-head a{color:#c9ccd6!important;text-transform:none;letter-spacing:0;font-weight:600;font-size:13px;text-decoration:none!important}\n.bd-guides-head a:hover{color:#fff!important}\n.bd-guide{display:flex;align-items:center;gap:16px;padding:16px;border-radius:18px;text-decoration:none!important;color:#fff!important;\n  transition:background .25s,transform .35s var(--bd-ease)}\n.bd-guide+.bd-guide{margin-top:6px}\n.bd-guide:hover{background:rgba(255,255,255,.08);transform:translateX(4px)}\n.bd-guide i{flex:none;display:grid;place-items:center;width:52px;height:52px;border-radius:16px;background:var(--c);box-shadow:0 10px 22px color-mix(in srgb,var(--c) 45%,transparent)}\n.bd-guide i svg{width:24px;height:24px;color:#fff}\n.bd-guide b{display:block;font:700 18px/1.25 var(--bd-serif)}\n.bd-guide span{display:block;margin-top:3px;font:400 13.5px/1.45 var(--bd-sans);color:#a9aebb}\n.bd-guide em{margin-left:auto;font-style:normal;color:#ffb199;font-size:20px;transition:transform .3s var(--bd-ease)}\n.bd-guide:hover em{transform:translateX(4px)}\n\n/* Topic tiles */\n.bd-topics{max-width:1170px;margin:38px auto 6px;padding:0 15px}\n.bd-section-head{display:flex;align-items:end;justify-content:space-between;gap:16px;margin-bottom:16px}\n.bd-section-head h2{font:800 30px/1.1 var(--bd-serif);letter-spacing:-.02em;margin:0;color:var(--bd-ink)}\n.bd-section-head p{margin:6px 0 0;color:var(--bd-muted);font:400 15px var(--bd-sans)}\n.bd-section-head a{font:600 14px var(--bd-sans);color:var(--bd-accent)!important;text-decoration:none!important;white-space:nowrap}\n.bd-topic-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}\n.bd-topic{position:relative;display:flex;align-items:center;gap:14px;padding:18px;border-radius:var(--bd-radius);background:var(--bd-surface);border:1px solid var(--bd-line);\n  text-decoration:none!important;overflow:hidden;transition:transform .3s var(--bd-ease),box-shadow .3s,border-color .3s}\n.bd-topic::before{content:'';position:absolute;inset:auto -40px -70px auto;width:140px;height:140px;border-radius:50%;background:var(--c);opacity:.1;transition:transform .5s,opacity .3s}\n.bd-topic:hover{transform:translateY(-4px);box-shadow:var(--bd-shadow-lg);border-color:transparent}\n.bd-topic:hover::before{transform:scale(2.6);opacity:.16}\n.bd-topic i{flex:none;display:grid;place-items:center;width:46px;height:46px;border-radius:14px;background:color-mix(in srgb,var(--c) 14%,var(--bd-surface));color:var(--c)}\n.bd-topic i svg{width:22px;height:22px}\n.bd-topic b{display:block;font:700 16px/1.25 var(--bd-sans);color:var(--bd-ink)}\n.bd-topic span{display:block;margin-top:3px;font:400 13px/1.4 var(--bd-sans);color:var(--bd-muted)}\n\n/* Section titles from the theme */\n.home-title,.widget-title,.title-wrap{background:transparent!important;border:0!important}\n.home-title span,.home-title,.widget-title h3.title,.title-wrap h3,.comments-title h3{background:transparent!important;color:var(--bd-ink)!important;\n  font:800 22px/1.2 var(--bd-serif)!important;letter-spacing:-.015em;text-transform:none!important;padding:0!important;height:auto!important}\n.home-title span::after,.home-title span::before,.home-title::after,.widget-title h3.title::after,.widget-title h3.title::before,.title-wrap h3::after,.title-wrap::after,.widget-title::after{display:none!important}\n.widget-title{margin-bottom:14px!important;padding-bottom:10px!important;border-bottom:2px solid var(--bd-line)!important;position:relative}\n.widget-title::before{content:'';position:absolute;left:0;bottom:-2px;width:42px;height:2px;background:var(--bd-accent)}\n.home-title{margin-bottom:18px!important}\n.widget-title h3.title,.home-title span{text-transform:capitalize!important}\nimg.bd-cover{opacity:1!important;visibility:visible!important}\n\n/* Post cards */\n.grid-posts{display:grid!important;grid-template-columns:repeat(2,1fr);gap:22px;margin:0!important}\n.grid-posts::before,.grid-posts::after{display:none!important}\n.index-post{display:flex!important;flex-direction:column;width:auto!important;float:none!important;margin:0!important;padding:0!important;background:var(--bd-surface);\n  border:1px solid var(--bd-line);border-radius:var(--bd-radius);overflow:hidden;transition:transform .35s var(--bd-ease),box-shadow .35s,border-color .35s}\n.index-post:hover{transform:translateY(-6px);box-shadow:var(--bd-shadow-lg);border-color:transparent}\n.index-post .post-image-wrap{width:100%!important;height:auto!important;margin:0!important;float:none!important;position:relative;aspect-ratio:16/10;overflow:hidden;\n  background:linear-gradient(135deg,#ffd9cc,#ffe9c2 50%,#e3dcff)}\n.index-post .post-image-link{width:100%!important;height:100%!important;display:block}\n.index-post .post-image-link img,.index-post .post-thumb{width:100%!important;height:100%!important;object-fit:cover;transition:transform .8s var(--bd-ease)}\n.index-post:hover .post-image-link img,.index-post:hover .post-thumb{transform:scale(1.07)}\n.index-post .post-thumb{opacity:1!important;object-position:center!important}\n.index-post .post-tag{position:absolute!important;top:14px!important;left:14px!important;bottom:auto!important;background:color-mix(in srgb,var(--bd-surface) 88%,transparent)!important;\n  -webkit-backdrop-filter:blur(8px);backdrop-filter:blur(8px);color:var(--bd-accent)!important;border-radius:999px!important;padding:0 12px!important;height:26px!important;\n  line-height:26px!important;font:700 11px/26px var(--bd-sans)!important;letter-spacing:.06em;text-transform:uppercase}\n.index-post .post-info{width:auto!important;float:none!important;padding:18px 20px 22px!important;margin:0!important;overflow:visible!important;display:flex;flex-direction:column;flex:1}\n.index-post .post-title{font-family:var(--bd-serif)!important;font-weight:700!important;font-size:21px!important;line-height:1.28!important;letter-spacing:-.01em;margin:0 0 10px!important}\n.index-post .post-title a{color:var(--bd-ink)!important;background:linear-gradient(var(--bd-accent),var(--bd-accent)) 0 100%/0 2px no-repeat;transition:background-size .4s}\n.index-post:hover .post-title a{background-size:100% 2px}\n.index-post .post-meta{margin-top:auto!important}\n.post-meta,.post-meta span,.post-meta a{color:var(--bd-muted)!important;font-family:var(--bd-sans)!important;font-size:13px!important}\n.bd-new{display:inline-block;margin-left:8px;padding:2px 8px;border-radius:999px;background:var(--bd-accent);color:#fff;font:700 10.5px/1.6 var(--bd-sans);letter-spacing:.06em;vertical-align:middle}\n@media (min-width:981px){\n  .home .grid-posts .index-post:first-child{grid-column:1/-1;flex-direction:row}\n  .home .grid-posts .index-post:first-child .post-image-wrap{width:56%!important;flex:none;aspect-ratio:auto;min-height:300px}\n  .home .grid-posts .index-post:first-child .post-info{justify-content:center;padding:30px 32px!important}\n  .home .grid-posts .index-post:first-child .post-title{font-size:30px!important;line-height:1.15!important}\n  .home .grid-posts .index-post:first-child .post-meta{margin-top:6px!important}\n}\n\n/* Pager */\n.blog-pager{display:flex!important;gap:8px;flex-wrap:wrap;margin-top:30px!important}\n.blog-pager .page-num,.blog-pager a,.blog-pager span.page-num{min-width:42px;height:42px!important;line-height:42px!important;border-radius:12px!important;\n  background:var(--bd-surface)!important;border:1px solid var(--bd-line)!important;color:var(--bd-ink)!important;font:600 14px/42px var(--bd-sans)!important;text-align:center;transition:all .2s}\n.blog-pager .page-active,.blog-pager a:hover{background:var(--bd-accent)!important;border-color:var(--bd-accent)!important;color:#fff!important}\n\n/* Sidebar */\n#sidebar-wrapper .widget{background:var(--bd-surface);border:1px solid var(--bd-line);border-radius:var(--bd-radius);padding:22px!important;box-shadow:var(--bd-shadow);margin-bottom:24px!important}\n#sidebar-wrapper .widget.Attribution,#sidebar-wrapper .widget.ReportAbuse{display:none!important}\n#sidebar-wrapper img{border-radius:12px}\n#random-posts li,#sidebar-wrapper .PopularPosts .post{border-color:var(--bd-line)!important}\n#random-posts li{display:flex;align-items:center;gap:12px;padding:10px 0!important;margin:0!important;overflow:hidden}\n#random-posts li:last-child{border-bottom:0!important}\n#random-posts li>a{flex:none}\n#random-posts img{width:64px!important;height:64px!important;padding:0!important;margin:0!important;float:none!important;object-fit:cover;background:var(--bd-surface-2)!important}\n#random-posts a,#sidebar-wrapper .widget a{color:var(--bd-ink)!important}\n#random-posts li>div>a,#random-posts li>span>a{font:600 14px/1.4 var(--bd-sans)!important;text-transform:none!important}\n#random-posts a:hover,#sidebar-wrapper .widget a:hover{color:var(--bd-accent)!important}\n#random-posts .random-summary,#random-posts .random-info span{display:none}\n#random-posts .random-info{color:var(--bd-muted)!important;font:500 12.5px var(--bd-sans)!important;margin-top:3px}\n#sidebar-wrapper input,#sidebar-wrapper textarea{border-radius:12px!important;border:1px solid var(--bd-line)!important;background:var(--bd-surface-2)!important;color:var(--bd-ink)!important;padding:10px 12px!important}\n#sidebar-wrapper input[type=button],#sidebar-wrapper input[type=submit],#sidebar-wrapper button,.contact-form-button{border-radius:12px!important;background:var(--bd-accent)!important;color:#fff!important;border:0!important;font-weight:700!important}\n#sidebar-wrapper .BlogArchive li span,#sidebar-wrapper .Label li span{border-radius:999px!important}\n#sidebar-wrapper .Text .widget-content{font:400 14.5px/1.6 var(--bd-sans)!important;color:var(--bd-muted)!important}\n.home #sidebar-wrapper #HTML1{display:none!important}\n.recent-posts-container{margin:0!important;padding:0!important;list-style:none!important}\n.recent-posts-container .recent-posts-list{display:grid!important;grid-template-columns:64px 1fr;column-gap:12px;align-items:center;font-size:0!important;\n  padding:10px 0!important;margin:0!important;border-bottom:1px solid var(--bd-line)!important;background:none!important;min-height:0!important}\n.recent-posts-container .recent-posts-list:last-child{border-bottom:0!important}\n.recent-posts-container .recent-posts-list::before{display:none!important}\n.recent-posts-container .recent-posts-list>a{grid-row:1/3}\n.recent-posts-container .recent-post-title{grid-column:2;grid-row:1}\n.recent-posts-container .recent-posts-details{grid-column:2;grid-row:2}\n.recent-posts-container .recent-post-thumb{display:block;width:64px!important;height:64px!important;border-radius:12px!important;float:none!important;margin:0!important;padding:0!important;border:0!important;object-fit:cover}\n.recent-posts-container .recent-post-title,.recent-posts-container .recent-post-title a{font:600 14px/1.4 var(--bd-sans)!important;text-transform:none!important;letter-spacing:0!important;margin:0!important}\n.recent-posts-container .recent-posts-list *{text-align:left!important}\n.recent-posts-container .recent-posts-details{font:500 12.5px/1.4 var(--bd-sans)!important;color:var(--bd-muted)!important;margin-top:3px!important}\n.recent-posts-container .recent-posts-details a,.recent-posts-container br{display:none!important}\n\n/* Article page */\n#bd-progress{position:fixed;top:0;left:0;height:3px;width:0;z-index:10000;background:linear-gradient(90deg,var(--bd-accent),var(--bd-gold));box-shadow:0 0 12px rgba(226,70,44,.5)}\n.item-post{background:var(--bd-surface);border:1px solid var(--bd-line);border-radius:24px;padding:38px 44px!important;box-shadow:var(--bd-shadow)}\n#breadcrumb,#breadcrumb a{font:500 13px var(--bd-sans)!important;color:var(--bd-muted)!important}\n#breadcrumb a:hover{color:var(--bd-accent)!important}\n.item-post h1.post-title{font-family:var(--bd-serif)!important;font-weight:800!important;font-size:clamp(30px,4vw,46px)!important;line-height:1.1!important;letter-spacing:-.03em!important;\n  color:var(--bd-ink)!important;margin:14px 0 16px!important}\n.bd-readtime{display:inline-flex;align-items:center;gap:6px;margin-left:12px;padding:3px 10px;border-radius:999px;background:var(--bd-accent-soft);color:var(--bd-accent);font:600 12.5px var(--bd-sans)}\n.post-body{font-family:var(--bd-sans)!important;color:var(--bd-ink)!important}\n.post-body .seo-post{font-size:18.5px!important;line-height:1.8!important;color:var(--bd-ink)}\n.post-body .seo-post>p.bd-updated{display:inline-flex;align-items:center;gap:8px;margin:0 0 18px!important;padding:5px 12px;border-radius:999px;\n  background:var(--bd-surface-2);border:1px solid var(--bd-line);font:600 13px/1.4 var(--bd-sans)!important;color:var(--bd-muted)!important}\n.post-body .seo-post>p.bd-updated::before{content:'';width:7px;height:7px;border-radius:50%;background:#16a34a;box-shadow:0 0 0 3px rgba(22,163,74,.18)}\n.post-body .seo-post>p.bd-updated em{font-style:normal}\n.post-body .seo-post>p.bd-quick{position:relative;padding:20px 24px 20px 26px;margin:0 0 1.4em!important;border-radius:18px;\n  background:linear-gradient(135deg,var(--bd-accent-soft),var(--bd-surface-2));border:1px solid color-mix(in srgb,var(--bd-accent) 22%,var(--bd-line));font-size:17.5px!important;line-height:1.7!important}\n.post-body .seo-post>p.bd-quick>strong:first-child{display:block;margin-bottom:6px;font:700 12.5px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:var(--bd-accent)}\n.post-body .seo-post p.bd-dropcap::first-letter{float:left;font:800 68px/.85 var(--bd-serif);color:var(--bd-accent);margin:8px 12px 0 0}\n.post-body h2{font:800 30px/1.2 var(--bd-serif)!important;letter-spacing:-.02em;color:var(--bd-ink)!important;margin:1.8em 0 .6em!important;scroll-margin-top:96px}\n.post-body h3{font:700 22px/1.3 var(--bd-serif)!important;color:var(--bd-ink)!important;scroll-margin-top:96px}\n.post-body a{color:var(--bd-accent)!important;text-decoration:underline;text-decoration-thickness:1px;text-underline-offset:3px}\n.post-body img{border-radius:16px;height:auto;max-width:100%}\n.post-body blockquote{border:0!important;border-left:4px solid var(--bd-accent)!important;background:var(--bd-surface-2)!important;border-radius:0 16px 16px 0;\n  padding:18px 24px!important;font:italic 500 21px/1.55 var(--bd-serif)!important;color:var(--bd-ink)!important;margin:1.6em 0!important}\n.post-body strong{color:var(--bd-ink)}\n.widget .post-body ol,.widget .post-body ul{padding-left:1.6em!important}\n.post-body li{margin:.35em 0!important;line-height:1.7!important;padding-left:.2em!important}\n.post-body ul li::marker,.post-body ol li::marker{color:var(--bd-accent);font-weight:700}\n.post-body ul li:before{color:var(--bd-accent)!important}\n.post-body table{border-collapse:separate!important;border-spacing:0;width:100%;border:1px solid var(--bd-line);border-radius:14px;overflow:hidden;font-size:16px}\n.post-body th{background:var(--bd-surface-2)!important;font-weight:700;text-align:left}\n.post-body th,.post-body td{padding:12px 14px!important;border:0!important;border-bottom:1px solid var(--bd-line)!important}\n.post-body tr:last-child td{border-bottom:0!important}\n.post-body h3.bd-faq-q{position:relative;margin:1.4em 0 .4em!important;padding:0 0 0 38px!important;font:700 19px/1.4 var(--bd-sans)!important}\n.post-body h3.bd-faq-q::before{content:'Q';position:absolute;left:0;top:0;display:grid;place-items:center;width:26px;height:26px;border-radius:8px;\n  background:var(--bd-accent);color:#fff;font:800 14px/1 var(--bd-sans)}\n.post-body .seo-toc{background:var(--bd-surface-2)!important;border:1px solid var(--bd-line)!important;border-left:4px solid var(--bd-accent)!important;border-radius:0 16px 16px 0!important;padding:18px 24px!important}\n.post-body .seo-toc p{font:700 12.5px var(--bd-sans)!important;letter-spacing:.12em;text-transform:uppercase;color:var(--bd-muted)}\n.post-body .seo-toc a{text-decoration:none;font-size:16px;font-weight:500}\n.post-body .seo-related{margin-top:48px;padding:26px;border-radius:20px;background:linear-gradient(135deg,var(--bd-accent-soft),var(--bd-surface-2));border:1px solid var(--bd-line)}\n.post-body .seo-related h3{margin:0 0 12px!important;font-size:22px!important}\n.post-body .seo-related ul{display:grid;grid-template-columns:1fr 1fr;gap:10px;list-style:none;margin:0!important;padding:0!important}\n.post-body .seo-related li{margin:0!important;padding:0!important}\n.post-body .seo-related li::before{display:none!important}\n.post-body .seo-related li a{display:block;height:100%;padding:14px 16px;border-radius:14px;background:var(--bd-surface);border:1px solid var(--bd-line);text-decoration:none!important;\n  font:600 15px/1.4 var(--bd-sans);color:var(--bd-ink)!important;transition:transform .25s,box-shadow .25s,color .2s}\n.post-body .seo-related li a:hover{transform:translateY(-3px);box-shadow:var(--bd-shadow);color:var(--bd-accent)!important}\n.post-body .seo-related p a{font-weight:600}\n.post-footer{margin-top:34px!important;padding-top:22px;border-top:1px solid var(--bd-line)}\n.post-labels{display:flex!important;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:14px!important}\n.post-labels>span{background:transparent!important;color:var(--bd-ink)!important;font:700 14px var(--bd-sans)!important;padding:0!important;height:auto!important;line-height:1.4!important}\n.post-labels>span::after,.post-labels>span::before{display:none!important}\n.post-labels .label-link{display:inline-block;padding:0 14px!important;height:34px;line-height:34px!important;background:var(--bd-accent-soft)!important;color:var(--bd-accent)!important;\n  font:600 13px/34px var(--bd-sans)!important;border:0!important}\n.post-share .share-links{display:flex!important;flex-wrap:wrap;gap:8px;margin:0!important}\n.post-share .share-links li{float:none!important;width:auto!important;margin:0!important}\n.post-share .share-links a{width:42px!important;height:42px!important;line-height:42px!important;border-radius:50%!important;\n  transition:transform .25s var(--bd-ease),box-shadow .25s}\n.post-share .share-links a:hover{transform:translateY(-3px);box-shadow:var(--bd-shadow)}\n.post-nav{display:grid!important;grid-template-columns:1fr 1fr;gap:12px;margin-top:22px!important;border:0!important;overflow:visible!important;height:auto!important;padding:0 0 2px!important}\n.post-nav li{float:none!important;width:auto!important;height:auto!important;margin:0!important;padding:0!important;overflow:visible!important}\n.post-nav a{display:block;height:100%;padding:14px 18px!important;border-radius:16px;border:1px solid var(--bd-line);background:var(--bd-surface-2);transition:border-color .2s,transform .25s var(--bd-ease)}\n.post-nav a:hover{border-color:var(--bd-accent);transform:translateY(-2px)}\n.post-nav span{font:700 11.5px var(--bd-sans)!important;letter-spacing:.1em;text-transform:uppercase;color:var(--bd-accent)!important}\n.post-nav p{margin:4px 0 0!important;font:600 15px/1.4 var(--bd-sans)!important;color:var(--bd-ink)!important}\n.post-labels a,.post-tag{border-radius:999px!important}\n.blog-post-comments{background:var(--bd-surface);border:1px solid var(--bd-line);border-radius:24px;padding:28px 34px!important;margin-top:24px!important}\n\n/* Closing call to action (not .bd-cta, which posts use for tool callouts) */\n.bd-closer{max-width:1170px;margin:56px auto 0;padding:0 15px}\n.bd-closer-inner{position:relative;overflow:hidden;display:flex;align-items:center;justify-content:space-between;gap:24px;flex-wrap:wrap;padding:38px 42px;border-radius:28px;\n  background:linear-gradient(120deg,var(--bd-accent),#f07a3a 55%,var(--bd-gold));color:#fff;isolation:isolate}\n.bd-closer-inner::after{content:'';position:absolute;z-index:-1;right:-80px;top:-120px;width:340px;height:340px;border-radius:50%;background:rgba(255,255,255,.14)}\n.bd-closer h2{margin:0;font:800 clamp(24px,3vw,34px)/1.15 var(--bd-serif);letter-spacing:-.02em;color:#fff}\n.bd-closer p{margin:8px 0 0;font:400 16px/1.55 var(--bd-sans);color:rgba(255,255,255,.88);max-width:560px}\n.bd-closer a{display:inline-flex;align-items:center;gap:8px;padding:15px 24px;border-radius:999px;background:#fff;color:var(--bd-deep)!important;font:700 15px var(--bd-sans);\n  text-decoration:none!important;box-shadow:0 14px 30px rgba(0,0,0,.18);transition:transform .25s var(--bd-ease)}\n.bd-closer a:hover{transform:translateY(-2px)}\n\n/* Footer */\n#footer-wrapper{background:var(--bd-deep)!important;border-radius:32px 32px 0 0;margin-top:56px!important;padding-top:24px}\n#footer-wrapper .widget-title{border-bottom-color:rgba(255,255,255,.1)!important}\n#footer-wrapper .widget-title h3.title{color:#fff!important}\n#footer-wrapper,#footer-wrapper a{color:#c5c9d3!important}\n#footer-wrapper a:hover{color:#ffb199!important}\n#footer-wrapper img{border-radius:12px;border-color:transparent!important;background:transparent!important}\n#footer-wrapper #random-posts li{border-color:rgba(255,255,255,.08)!important}\n#sub-footer-wrapper{background:#0a0b0f!important;border-top:1px solid rgba(255,255,255,.06)}\n#sub-footer-wrapper,#sub-footer-wrapper a{color:#8d92a0!important;font-family:var(--bd-sans)!important}\n\n/* Back to top */\n#bd-top{position:fixed;right:22px;bottom:22px;z-index:998;width:48px;height:48px;border-radius:50%;border:0;background:var(--bd-accent);color:#fff;cursor:pointer;\n  box-shadow:0 12px 28px rgba(226,70,44,.4);display:grid;place-items:center;opacity:0;transform:translateY(16px) scale(.9);pointer-events:none;transition:all .3s}\n#bd-top.bd-show{opacity:1;transform:none;pointer-events:auto}\n#bd-top svg{width:20px;height:20px}\n#back-top,.back-top{display:none!important}\n\n/* Reveal on scroll */\n.bd-js .bd-reveal{opacity:0;transform:translateY(26px);transition:opacity .7s ease,transform .7s var(--bd-ease)}\n.bd-js .bd-reveal.bd-in{opacity:1;transform:none}\n\n/* Dark mode: surfaces the theme paints white */\nhtml.bd-dark #content-wrapper,html.bd-dark #main-wrapper,html.bd-dark #sidebar-wrapper,html.bd-dark .post-body,html.bd-dark .widget-content,\nhtml.bd-dark #break-wrapper-outer,html.bd-dark .header-header{background:transparent!important;color:var(--bd-ink)!important}\nhtml.bd-dark .post-body *:not(a):not(.seo-toc):not(.seo-related):not(blockquote):not(img):not(.bd-quick):not(.bd-updated):not(th){color:inherit!important;background-color:transparent}\nhtml.bd-dark #random-posts li,html.bd-dark .widget li{border-color:var(--bd-line)!important}\nhtml.bd-dark .slide-menu-toggle,html.bd-dark .slide-menu-toggle:before{color:var(--bd-ink)!important}\n\n/* Instant search results */\n.bd-search-results{width:min(640px,100%);margin-top:10px;border-radius:18px;background:var(--bd-surface);border:1px solid var(--bd-line);box-shadow:var(--bd-shadow-lg);\n  overflow:hidden;max-height:60vh;overflow-y:auto}\n.bd-search-results:empty{display:none}\n.bd-search-results a{display:flex;align-items:center;gap:12px;padding:12px 16px;text-decoration:none!important;color:var(--bd-ink)!important;border-bottom:1px solid var(--bd-line)}\n.bd-search-results a:last-child{border-bottom:0}\n.bd-search-results a:hover,.bd-search-results a.bd-sel{background:var(--bd-accent-soft)}\n.bd-search-results img{flex:none;width:48px;height:48px;border-radius:10px;object-fit:cover}\n.bd-search-results b{display:block;font:600 15px/1.35 var(--bd-sans)}\n.bd-search-results small{display:block;margin-top:2px;font:500 12.5px var(--bd-sans);color:var(--bd-muted)}\n.bd-search-results .bd-empty{padding:16px;font:500 14px var(--bd-sans);color:var(--bd-muted)}\n\n/* Article reading bar, contents panel and \"up next\" card */\n.bd-readbar{position:fixed;left:50%;bottom:16px;z-index:997;display:flex;align-items:center;gap:6px;width:min(620px,calc(100vw - 104px));padding:6px;border-radius:999px;\n  background:color-mix(in srgb,var(--bd-surface) 92%,transparent);-webkit-backdrop-filter:blur(14px);backdrop-filter:blur(14px);border:1px solid var(--bd-line);box-shadow:var(--bd-shadow-lg);\n  transform:translate(-50%,140%);transition:transform .45s var(--bd-ease)}\n.bd-readbar.bd-show{transform:translate(-50%,0)}\n.bd-readbar button,.bd-readbar a{flex:none;display:inline-flex;align-items:center;gap:6px;height:38px;padding:0 14px;border:0;border-radius:999px;cursor:pointer;\n  font:600 13.5px var(--bd-sans);text-decoration:none!important;white-space:nowrap}\n.bd-readbar .bd-rb-toc{background:var(--bd-surface-2);color:var(--bd-ink)!important;border:1px solid var(--bd-line)}\n.bd-readbar .bd-rb-next{background:var(--bd-accent);color:#fff!important}\n.bd-readbar svg{width:16px;height:16px}\n.bd-readbar .bd-rb-now{flex:1;min-width:0;padding:0 6px;font:500 13.5px var(--bd-sans);color:var(--bd-muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}\n.bd-readbar .bd-rb-now b{color:var(--bd-ink);font-weight:600}\n.bd-toc-panel{position:fixed;left:50%;bottom:76px;z-index:997;width:min(620px,calc(100vw - 32px));max-height:min(62vh,520px);overflow-y:auto;padding:10px;border-radius:20px;\n  background:var(--bd-surface);border:1px solid var(--bd-line);box-shadow:var(--bd-shadow-lg);transform:translate(-50%,12px);opacity:0;pointer-events:none;transition:all .3s var(--bd-ease)}\n.bd-toc-panel.bd-open{transform:translate(-50%,0);opacity:1;pointer-events:auto}\n.bd-toc-panel p{margin:6px 10px 8px;font:700 12px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:var(--bd-muted)}\n.bd-toc-panel a{display:flex;gap:10px;padding:10px 12px;border-radius:12px;color:var(--bd-ink)!important;text-decoration:none!important;font:500 14.5px/1.4 var(--bd-sans)}\n.bd-toc-panel a i{flex:none;font-style:normal;font-weight:700;color:var(--bd-muted);min-width:18px}\n.bd-toc-panel a:hover{background:var(--bd-surface-2)}\n.bd-toc-panel a.bd-cur{background:var(--bd-accent-soft);color:var(--bd-accent)!important}\n.bd-toc-panel a.bd-cur i{color:var(--bd-accent)}\n.bd-upnext{position:fixed;right:22px;bottom:84px;z-index:996;width:330px;display:flex;gap:14px;align-items:center;padding:14px 16px;border-radius:20px;background:var(--bd-surface);\n  border:1px solid var(--bd-line);box-shadow:var(--bd-shadow-lg);text-decoration:none!important;transform:translateY(20px);opacity:0;pointer-events:none;transition:all .45s var(--bd-ease)}\n.bd-upnext.bd-show{transform:none;opacity:1;pointer-events:auto}\n.bd-upnext img{flex:none;width:64px;height:64px;border-radius:14px;object-fit:cover}\n.bd-upnext small{display:block;font:700 11.5px var(--bd-sans);letter-spacing:.1em;text-transform:uppercase;color:var(--bd-accent)}\n.bd-upnext b{display:block;margin-top:3px;font:700 15.5px/1.35 var(--bd-serif);color:var(--bd-ink)}\n.bd-upnext button{position:absolute;top:6px;right:8px;width:26px;height:26px;border:0;border-radius:50%;background:transparent;color:var(--bd-muted);font-size:18px;line-height:1;cursor:pointer}\n.bd-upnext button:hover{background:var(--bd-surface-2);color:var(--bd-ink)}\n.post-body .bd-inline-read{display:flex;align-items:center;gap:16px;margin:2em 0;padding:16px 18px;border-radius:18px;background:var(--bd-surface-2);border:1px solid var(--bd-line);\n  text-decoration:none!important;transition:border-color .2s,transform .3s var(--bd-ease)}\n.post-body .bd-inline-read:hover{border-color:var(--bd-accent);transform:translateY(-2px)}\n.post-body .bd-inline-read img{flex:none;width:72px;height:72px;border-radius:14px;object-fit:cover;margin:0!important}\n.post-body .bd-inline-read small{display:block;font:700 11.5px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:var(--bd-accent)}\n.post-body .bd-inline-read b{display:block;margin-top:4px;font:700 18px/1.35 var(--bd-serif);color:var(--bd-ink)}\n\n/* Continue reading, read chips, topic and error pages */\n.bd-resume{max-width:1170px;margin:16px auto 0;padding:0 15px}\n.bd-resume a{display:flex;align-items:center;gap:14px;padding:12px 16px;border-radius:18px;background:var(--bd-surface);border:1px solid var(--bd-line);box-shadow:var(--bd-shadow);\n  text-decoration:none!important;color:var(--bd-ink)!important}\n.bd-resume a:hover{border-color:var(--bd-accent)}\n.bd-resume i{flex:none;position:relative;width:44px;height:44px;border-radius:50%;background:conic-gradient(var(--bd-accent) calc(var(--p)*1%),var(--bd-line) 0)}\n.bd-resume i::after{content:'';position:absolute;inset:5px;border-radius:50%;background:var(--bd-surface)}\n.bd-resume span{flex:1;min-width:0}\n.bd-resume small{display:block;font:700 11.5px var(--bd-sans);letter-spacing:.1em;text-transform:uppercase;color:var(--bd-accent)}\n.bd-resume b{display:block;margin-top:2px;font:600 15px/1.35 var(--bd-sans);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}\n.bd-resume em{flex:none;font:600 13px var(--bd-sans);font-style:normal;color:var(--bd-muted)}\n.bd-resume button{flex:none;width:30px;height:30px;border:0;border-radius:50%;background:transparent;color:var(--bd-muted);font-size:18px;cursor:pointer}\n.bd-read-chip{position:absolute;top:14px;right:14px;z-index:2;display:inline-flex;align-items:center;gap:5px;height:26px;padding:0 10px;border-radius:999px;\n  background:rgba(18,20,27,.72);color:#fff;font:700 11px/26px var(--bd-sans);letter-spacing:.06em;text-transform:uppercase;-webkit-backdrop-filter:blur(6px);backdrop-filter:blur(6px)}\n.bd-topic-head{position:relative;overflow:hidden;margin:0 0 22px;padding:28px 30px;border-radius:24px;background:var(--bd-surface);border:1px solid var(--bd-line);box-shadow:var(--bd-shadow);isolation:isolate}\n.bd-topic-head::after{content:'';position:absolute;z-index:-1;right:-60px;top:-80px;width:240px;height:240px;border-radius:50%;background:var(--c);opacity:.12}\n.bd-topic-head small{font:700 12px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:var(--c)}\n.bd-topic-head h1{margin:6px 0 8px;font:800 clamp(28px,4vw,40px)/1.1 var(--bd-serif);letter-spacing:-.02em;color:var(--bd-ink)}\n.bd-topic-head p{margin:0;max-width:560px;font:400 16px/1.6 var(--bd-sans);color:var(--bd-muted)}\n.bd-topic-head a{display:inline-flex;margin-top:14px;padding:10px 16px;border-radius:999px;background:var(--c);color:#fff!important;font:700 14px var(--bd-sans);text-decoration:none!important}\n.queryMessage{border-radius:14px!important;background:var(--bd-surface-2)!important;border:1px solid var(--bd-line)!important}\n.bd-404{max-width:720px;margin:0 auto;text-align:center}\n.bd-404 .bd-search{margin:22px auto 0;background:var(--bd-surface);border-color:var(--bd-line)}\n.bd-404 .bd-search input{color:var(--bd-ink)}\n.bd-404-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-top:26px;text-align:left}\n.bd-404-grid a{display:block;padding:16px;border-radius:16px;background:var(--bd-surface);border:1px solid var(--bd-line);text-decoration:none!important;color:var(--bd-ink)!important;font:600 15px/1.4 var(--bd-sans)}\n.bd-404-grid a:hover{border-color:var(--bd-accent);color:var(--bd-accent)!important}\n.bd-404 h3.bd-404-sub{margin:34px 0 0;font:800 22px var(--bd-serif);color:var(--bd-ink)}\n.errorWrap h3{font-family:var(--bd-serif)!important;color:var(--bd-accent)!important}\n.errorWrap h4{font-family:var(--bd-serif)!important;color:var(--bd-ink)!important}\n\n/* Editorial article page */\nhtml.bd-article #sidebar-wrapper,html.bd-article #breadcrumb,html.bd-article .item-post .post-meta,\nhtml.bd-article .post-body .seo-post>p.bd-updated{display:none!important}\nhtml.bd-article #main-wrapper{float:none!important;width:100%!important;max-width:800px;margin:0 auto!important;padding:0!important}\nhtml.bd-article .item-post{padding:48px 60px!important}\nhtml.bd-article .item-post>.post-body{border-top:0!important;padding-top:0!important}\n.bd-kicker{display:inline-block;margin:0 0 14px;font:700 12.5px/1 var(--bd-sans);letter-spacing:.14em;text-transform:uppercase;color:var(--bd-accent)!important;text-decoration:none!important}\nhtml.bd-article .item-post h1.post-title{font-size:clamp(34px,5.2vw,54px)!important;line-height:1.06!important;margin:0 0 22px!important}\n.bd-byline{display:flex;align-items:center;gap:12px;padding:0 0 26px;margin:0 0 30px;border-bottom:1px solid var(--bd-line)}\n.bd-byline .bd-mark{display:grid;place-items:center;width:40px;height:40px;border-radius:12px;background:linear-gradient(135deg,var(--bd-accent),var(--bd-gold));color:#fff;font:italic 800 22px/1 var(--bd-serif)}\n.bd-byline b{display:block;font:700 15px/1.3 var(--bd-sans);color:var(--bd-ink)}\n.bd-byline small{display:block;font:500 13.5px/1.4 var(--bd-sans);color:var(--bd-muted)}\nhtml.bd-article .post-body .seo-post{font-family:'Newsreader',Georgia,'Times New Roman',serif!important;font-size:20.5px!important;line-height:1.72!important;\n  font-optical-sizing:auto;font-kerning:normal;hanging-punctuation:first}\nhtml.bd-article .post-body .seo-post p{margin:0 0 1.15em!important}\nhtml.bd-article .post-body .seo-post p.bd-quick,html.bd-article .post-body .bd-calc,html.bd-article .post-body .seo-toc,html.bd-article .post-body table,\nhtml.bd-article .post-body .seo-related,html.bd-article .post-body .bd-inline-read{font-family:var(--bd-sans)!important}\nhtml.bd-article .post-body .seo-post p.bd-quick{font-size:17px!important}\nhtml.bd-article .post-body .seo-post p.bd-dropcap::first-letter{font-size:76px;margin-top:10px}\nhtml.bd-article .post-body .separator{margin:1.8em 0!important}\nhtml.bd-article .post-body .separator a{display:block;margin:0!important}\nhtml.bd-article .post-body .seo-post img{display:block;width:100%;height:auto;margin:0 auto;border-radius:14px;background:var(--bd-surface-2)}\nhtml.bd-article .post-body h2{font-size:32px!important;margin:1.9em 0 .55em!important}\nhtml.bd-article .post-body h3{font-size:23px!important}\nhtml.bd-article .post-body blockquote{font-size:24px!important}\n\n/* Article page, editorial details */\nhtml.bd-article .item-post,html.bd-article .blog-post-comments{background:transparent!important;border:0!important;box-shadow:none!important;border-radius:0!important}\nhtml.bd-article .item-post{padding:34px 0 0!important}\nhtml.bd-article .blog-post-comments{padding:30px 0 0!important;border-top:1px solid var(--bd-line)!important}\nhtml.bd-article .blog-post-comments .comments-title h3,html.bd-article #comments>h3.title{text-align:left!important;font:800 24px var(--bd-serif)!important}\nhtml.bd-article #comments .comment-form>p{font:400 15px var(--bd-sans)!important;color:var(--bd-muted)!important}\nhtml.bd-article .post-body .seo-post>p.bd-quick{background:none!important;border:0!important;border-left:4px solid var(--bd-accent)!important;border-radius:0!important;\n  padding:2px 0 2px 22px!important;margin:4px 0 1.8em!important;font-size:18px!important;line-height:1.65!important}\nhtml.bd-article .post-body .seo-post>p.bd-quick>strong:first-child{margin-bottom:4px}\nhtml.bd-article .post-body ul,html.bd-article .post-body ol{margin:.5em 0 1.4em!important;padding-left:1.35em!important}\nhtml.bd-article .post-body ul{list-style:disc outside!important}\nhtml.bd-article .post-body ol{list-style:decimal outside!important}\nhtml.bd-article .post-body li{margin:.5em 0!important;padding-left:.25em!important}\nhtml.bd-article .post-body ul li:before{display:none!important}\nhtml.bd-article .post-body ul li::marker{color:var(--bd-accent);font-size:.9em}\nhtml.bd-article .post-body ol li::marker{color:var(--bd-accent);font:700 .85em var(--bd-sans)}\nhtml.bd-article .post-body .seo-toc{border:0!important;border-top:1px solid var(--bd-line)!important;border-bottom:1px solid var(--bd-line)!important;border-radius:0!important;\n  background:none!important;padding:18px 0!important;margin:1.6em 0 2em!important}\nhtml.bd-article .post-body .seo-toc ol{columns:2;column-gap:36px;margin:8px 0 0!important}\nhtml.bd-article .post-body .seo-toc li{break-inside:avoid;margin:0 0 .55em!important;font:500 15.5px/1.4 var(--bd-sans)}\nhtml.bd-article .post-body .seo-toc li::marker{color:var(--bd-muted)}\nhtml.bd-article .post-body .seo-toc a{color:var(--bd-ink)!important;text-decoration:none!important}\nhtml.bd-article .post-body .seo-toc a:hover{color:var(--bd-accent)!important}\nhtml.bd-article .post-body .seo-toc.bd-toc-folded li:nth-child(n+6){display:none}\n.bd-toc-more{margin-top:6px;padding:8px 14px;border-radius:999px;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink);font:600 13.5px var(--bd-sans);cursor:pointer}\n.bd-toc-more:hover{border-color:var(--bd-accent);color:var(--bd-accent)}\n.bd-end{display:inline-block;width:.55em;height:.55em;margin-left:.4em;border-radius:2px;background:var(--bd-accent);vertical-align:.05em}\n.bd-pubnote{display:flex;gap:16px;align-items:flex-start;margin:40px 0 8px;padding:22px 0;border-top:2px solid var(--bd-ink);border-bottom:1px solid var(--bd-line)}\n.bd-pubnote .bd-mark{flex:none;display:grid;place-items:center;width:48px;height:48px;border-radius:14px;background:linear-gradient(135deg,var(--bd-accent),var(--bd-gold));color:#fff;font:italic 800 26px/1 var(--bd-serif)}\n.bd-pubnote b{display:block;font:800 18px var(--bd-serif);color:var(--bd-ink)}\n.bd-pubnote p{margin:4px 0 10px!important;font:400 15px/1.55 var(--bd-sans);color:var(--bd-muted)}\n.bd-pub-links{display:flex;flex-wrap:wrap;gap:8px}\n.bd-pub-links a{padding:6px 14px;border-radius:999px;background:var(--bd-accent-soft);color:var(--bd-accent)!important;font:700 13.5px var(--bd-sans);text-decoration:none!important}\nhtml.bd-article .post-body .seo-related{background:none!important;border:0!important;border-top:1px solid var(--bd-line)!important;border-radius:0!important;padding:24px 0 0!important}\nhtml.bd-article .post-body .seo-related ul{display:block!important;padding-left:0!important;list-style:none!important}\nhtml.bd-article .post-body .seo-related li{border-bottom:1px solid var(--bd-line)}\nhtml.bd-article .post-body .seo-related li a{border:0!important;background:none!important;padding:13px 0!important;border-radius:0!important;font:700 17px/1.35 var(--bd-serif)!important;box-shadow:none!important;transform:none!important}\nhtml.bd-article .post-body .seo-related li::marker{content:''}\n#related-wrap .title-wrap h3{text-align:left!important;font:800 24px var(--bd-serif)!important}\n#related-wrap .related-posts .post-tag{border-radius:999px!important;background:color-mix(in srgb,var(--bd-surface) 88%,transparent)!important;color:var(--bd-accent)!important;\n  font:700 10.5px/22px var(--bd-sans)!important;padding:0 9px!important;height:22px!important;top:8px!important;left:8px!important}\n#related-wrap .related-posts .post-tag::before,#related-wrap .related-posts .post-tag::after{display:none!important}\n#related-wrap .related-posts .post-title,#related-wrap .related-posts .post-title a{font:700 16px/1.35 var(--bd-serif)!important;color:var(--bd-ink)!important}\n#related-wrap .post-image-wrap,#related-wrap .post-image-wrap img{border-radius:12px!important}\n.bd-rail{position:fixed;top:120px;left:max(16px,calc(50% - 400px - 250px));width:220px;max-height:calc(100vh - 160px);overflow-y:auto;scrollbar-width:thin;z-index:5;\n  opacity:0;pointer-events:none;transform:translateX(-8px);transition:opacity .3s,transform .3s var(--bd-ease);display:none}\n.bd-rail.bd-show{opacity:1;pointer-events:auto;transform:none}\n.bd-rail p{margin:0 0 8px;font:700 11.5px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:var(--bd-muted)}\n.bd-rail a{display:block;padding:6px 0 6px 12px;border-left:2px solid var(--bd-line);color:var(--bd-muted)!important;font:500 13.5px/1.4 var(--bd-sans);text-decoration:none!important;transition:color .2s,border-color .2s}\n.bd-rail a:hover{color:var(--bd-ink)!important}\n.bd-rail a.bd-cur{border-left-color:var(--bd-accent);color:var(--bd-ink)!important;font-weight:700}\n.bd-rail a.bd-rail-next{margin-top:16px;border-left:0;padding:12px 14px;border-radius:14px;background:var(--bd-surface);border:1px solid var(--bd-line);color:var(--bd-ink)!important;font:700 14px/1.35 var(--bd-serif)}\n.bd-rail a.bd-rail-next small{display:block;margin-bottom:3px;font:700 11px var(--bd-sans);letter-spacing:.1em;text-transform:uppercase;color:var(--bd-accent)}\n@media (min-width:1280px){.bd-rail{display:block}html.bd-article .bd-readbar{display:none!important}}\n\n/* Article illustrations */\n.post-body .bd-ill{margin:2.2em 0 1.6em!important;padding:0!important;border:0!important}\n.post-body .bd-ill svg{display:block;width:100%;height:auto;border-radius:20px;overflow:hidden}\n.bd-ill .ill-bg{fill:color-mix(in srgb,var(--c) 9%,var(--bd-surface))}\n.bd-ill .ill-bgc{fill:color-mix(in srgb,var(--c) 9%,var(--bd-surface))}\n.bd-ill .ill-soft{fill:var(--c);opacity:.08}\n.bd-ill .ill-main{fill:var(--c)}\n.bd-ill .ill-light{fill:color-mix(in srgb,var(--c) 30%,var(--bd-surface))}\n.bd-ill .ill-face{fill:var(--bd-surface)}\n.bd-ill .ill-bar{fill:var(--c);opacity:.45}\n.bd-ill .ill-glow{fill:var(--c);opacity:.15}\n.bd-ill .ill-star,.bd-ill .ill-dot{fill:var(--c)}\n.bd-ill .ill-line,.bd-ill .ill-stem{fill:none;stroke:var(--c);stroke-width:6;stroke-linecap:round;stroke-linejoin:round}\n.bd-ill .ill-tick{fill:none;stroke:var(--bd-surface);stroke-width:9;stroke-linecap:round;stroke-linejoin:round}\n.bd-ill .ill-face~.ill-tick,.bd-ill rect.ill-face+rect+.ill-tick{stroke:var(--c)}\n.bd-ill .ill-axis,.bd-ill .ill-link,.bd-ill .ill-meridian,.bd-ill .ill-ticks line,.bd-ill .ill-ray{fill:none;stroke:color-mix(in srgb,var(--c) 45%,var(--bd-surface));stroke-width:3;stroke-linecap:round}\n.bd-ill .ill-hand{stroke:var(--c);stroke-width:8;stroke-linecap:round}.bd-ill .ill-hand.thin{stroke-width:4}\n.bd-ill .ill-ring{fill:none;stroke:var(--c);stroke-width:3}\n.bd-ill .ill-sym{font:800 64px var(--bd-serif);fill:var(--c)}.bd-ill .ill-sym.small{font-size:26px;fill:var(--c)}.bd-ill .ill-sym.small.big{font-size:40px}\n.bd-ill g.a-coin .ill-sym{fill:var(--c)}\n.bd-ill .ill-z{font:italic 800 26px var(--bd-serif);fill:var(--c)}\n.bd-ill [class*=\"a-\"]{transform-box:fill-box;transform-origin:center;animation-play-state:paused;animation-iteration-count:infinite}\n.bd-ill.bd-ill-on [class*=\"a-\"]{animation-play-state:running}\n.bd-ill .d2{animation-delay:.6s}.bd-ill .d3{animation-delay:1.2s}\n.bd-ill .a-float{animation:ill-float 5s ease-in-out infinite}\n.bd-ill .a-pulse{animation:ill-pulse 2.4s ease-in-out infinite}\n.bd-ill .a-beat{animation:ill-beat 1.4s ease-in-out infinite}\n.bd-ill .a-blink{animation:ill-blink 2.2s ease-in-out infinite}\n.bd-ill .a-spin{transform-origin:bottom center;animation:ill-spin 6s linear infinite}.bd-ill .a-spin.slow{animation-duration:36s}\n.bd-ill .a-rise{transform-origin:bottom center;animation:ill-rise 3.2s var(--bd-ease) infinite alternate}\n.bd-ill .a-draw{stroke-dasharray:600;stroke-dashoffset:600;animation:ill-draw 3.6s ease-in-out infinite}\n.bd-ill .a-drop{animation:ill-drop 4s var(--bd-ease) infinite}\n.bd-ill .a-ping{animation:ill-ping 2.6s ease-out infinite}\n.bd-ill .a-sway{transform-origin:left center;animation:ill-sway 4s ease-in-out infinite alternate}.bd-ill .a-sway.d2{transform-origin:right center}\n.bd-ill .a-shoot{animation:ill-shoot 3s var(--bd-ease) infinite}\n.bd-ill .a-climb{animation:ill-climb 5s ease-in-out infinite}\n.bd-ill .a-squash{animation:ill-squash 4s ease-in-out infinite alternate}\n.bd-ill .a-orbit{animation:ill-orbit 6s linear infinite}\n.bd-ill .a-flow{animation:ill-flow 2.4s linear infinite}.bd-ill .a-flow.rev{animation-name:ill-flowr}\n.bd-ill .a-tilt{transform-origin:center top;animation:ill-tilt 4s ease-in-out infinite alternate}\n.bd-ill .a-pop{animation:ill-pop 3s ease-in-out infinite}\n.bd-ill .a-flip{transform-origin:left center;animation:ill-flip 3.4s ease-in-out infinite}\n.bd-ill .a-coin{animation:ill-coin 4s ease-in-out infinite}\n.bd-ill .a-swipe{animation:ill-swipe 3.6s var(--bd-ease) infinite}\n.bd-ill .a-fill{transform-origin:left center;animation:ill-fill 3.6s ease-in-out infinite}\n.bd-ill .a-up{animation:ill-up 3s ease-in-out infinite}\n@keyframes ill-float{50%{transform:translateY(-10px)}}\n@keyframes ill-pulse{50%{transform:scale(1.12)}}\n@keyframes ill-beat{0%,100%{transform:scale(1)}15%{transform:scale(1.1)}30%{transform:scale(.97)}45%{transform:scale(1.06)}}\n@keyframes ill-blink{50%{opacity:.3}}\n@keyframes ill-spin{to{transform:rotate(360deg)}}\n@keyframes ill-rise{from{transform:scaleY(.35)}to{transform:scaleY(1)}}\n@keyframes ill-draw{0%{stroke-dashoffset:600}55%,80%{stroke-dashoffset:0}100%{stroke-dashoffset:-600}}\n@keyframes ill-drop{0%{transform:translateY(-60px);opacity:0}25%,85%{transform:none;opacity:1}100%{opacity:0}}\n@keyframes ill-ping{0%{transform:scale(.9);opacity:.8}100%{transform:scale(1.5);opacity:0}}\n@keyframes ill-sway{to{transform:rotate(8deg)}}\n@keyframes ill-shoot{0%{transform:translate(120px,-60px);opacity:0}30%,80%{transform:none;opacity:1}100%{opacity:0}}\n@keyframes ill-climb{0%{transform:translate(198px,186px)}25%{transform:translate(254px,156px)}50%{transform:translate(310px,126px)}75%{transform:translate(366px,96px)}100%{transform:translate(422px,66px)}}\n@keyframes ill-squash{to{transform:scaleX(.35)}}\n@keyframes ill-orbit{from{transform:rotate(0) translateX(80px)}to{transform:rotate(360deg) translateX(80px)}}\n@keyframes ill-flow{from{transform:translateX(0)}to{transform:translateX(80px)}}\n@keyframes ill-flowr{from{transform:translateX(0)}to{transform:translateX(-80px)}}\n@keyframes ill-tilt{from{transform:rotate(-6deg)}to{transform:rotate(6deg)}}\n@keyframes ill-pop{0%,100%{transform:scale(.92);opacity:.75}50%{transform:scale(1);opacity:1}}\n@keyframes ill-flip{0%,20%{transform:scaleX(1)}50%{transform:scaleX(-1)}80%,100%{transform:scaleX(1)}}\n@keyframes ill-coin{0%,100%{transform:scaleX(1)}50%{transform:scaleX(.15)}}\n@keyframes ill-swipe{0%{transform:translateX(-30px) rotate(-4deg)}50%{transform:translateX(30px) rotate(3deg)}100%{transform:translateX(-30px) rotate(-4deg)}}\n@keyframes ill-fill{0%{transform:scaleX(.05)}60%,100%{transform:scaleX(1)}}\n@keyframes ill-up{0%{transform:translateY(10px);opacity:0}50%{opacity:1}100%{transform:translateY(-18px);opacity:0}}\n@media (prefers-reduced-motion:reduce){.bd-ill [class*=\"a-\"]{animation:none!important;stroke-dashoffset:0!important;opacity:1!important}}\n\n/* Magazine front page */\n.home #break-wrapper-outer{display:none!important}\n.bd-front{max-width:1170px;margin:22px auto 0;padding:0 15px;display:grid;gap:22px}\n.bd-front *{box-sizing:border-box}\n.bd-mast{display:flex;align-items:center;gap:18px;flex-wrap:wrap;padding-bottom:14px;border-bottom:1px solid var(--bd-line)}\n.bd-date{font:700 13px var(--bd-sans);letter-spacing:.04em;color:var(--bd-ink)}\n.bd-tagline{font:italic 700 17px var(--bd-serif);color:var(--bd-muted)}\n.bd-mast-search{margin-left:auto}\n.bd-mast-search input{width:220px;max-width:100%;height:38px;padding:0 14px;border-radius:999px;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink);font:500 14px var(--bd-sans)}\n.bd-secnav{display:flex;gap:4px;overflow-x:auto;scrollbar-width:none;border-bottom:2px solid var(--bd-ink);padding-bottom:10px;margin-top:-8px}\n.bd-secnav::-webkit-scrollbar{display:none}\n.bd-secnav a{flex:none;padding:6px 12px;border-radius:999px;font:700 14px var(--bd-sans);color:var(--bd-ink)!important;text-decoration:none!important}\n.bd-secnav a:hover{background:var(--bd-accent-soft);color:var(--bd-accent)!important}\n.bd-lead-grid{display:grid;grid-template-columns:minmax(0,1.9fr) minmax(0,1fr) minmax(0,1fr);gap:28px;align-items:start}\n.bd-lead a,.bd-story,.bd-latest a{text-decoration:none!important;color:var(--bd-ink)!important;display:block}\n.bd-lead-img,.bd-story-img{display:block;overflow:hidden;border-radius:16px;background:var(--bd-surface-2)}\n.bd-lead-img{aspect-ratio:16/10}.bd-story-img{aspect-ratio:16/10;margin-bottom:10px}\n.bd-lead-img img,.bd-story-img img{width:100%;height:100%;object-fit:cover;transition:transform .8s var(--bd-ease)}\n.bd-lead a:hover img,.bd-story:hover img{transform:scale(1.04)}\n.bd-kick{display:block;margin:14px 0 6px;font:700 11.5px var(--bd-sans);letter-spacing:.12em;text-transform:uppercase;color:var(--bd-accent)}\n.bd-story .bd-kick{margin-top:0}\n.bd-lead h1{margin:0 0 10px;font:800 clamp(28px,3.4vw,42px)/1.08 var(--bd-serif);letter-spacing:-.025em;text-wrap:balance}\n.bd-lead p{margin:0 0 10px;font:400 17px/1.55 var(--bd-sans);color:var(--bd-muted);max-width:60ch}\n.bd-meta,.bd-latest small{font:600 12.5px var(--bd-sans);color:var(--bd-muted)}\n.bd-lead a:hover h1,.bd-story:hover b,.bd-latest a:hover b{color:var(--bd-accent)}\n.bd-second{display:grid;gap:24px}\n.bd-story b{display:block;font:700 20px/1.25 var(--bd-serif);letter-spacing:-.01em;text-wrap:balance}\n.bd-latest{border-left:1px solid var(--bd-line);padding-left:24px}\n.bd-latest h2{margin:0 0 6px;font:800 13px var(--bd-sans)!important;letter-spacing:.14em;text-transform:uppercase;color:var(--bd-ink)}\n.bd-latest-list{list-style:none;margin:0;padding:0;counter-reset:bdl}\n.bd-latest-list li{padding:12px 0;border-bottom:1px solid var(--bd-line)}\n.bd-latest-list li:last-child{border-bottom:0}\n.bd-latest b{display:block;margin-top:3px;font:700 17px/1.3 var(--bd-serif);text-wrap:balance}\n.bd-start{display:grid;grid-template-columns:auto repeat(3,minmax(0,1fr));gap:12px;align-items:center;padding:14px 16px;border-radius:18px;background:var(--bd-surface);border:1px solid var(--bd-line)}\n.bd-start-label{font:700 12px var(--bd-sans);letter-spacing:.1em;text-transform:uppercase;color:var(--bd-muted);padding-right:6px}\n.bd-start-card{display:flex;align-items:center;gap:12px;padding:8px 10px;border-radius:14px;text-decoration:none!important;color:var(--bd-ink)!important;transition:background .2s}\n.bd-start-card:hover{background:var(--bd-surface-2)}\n.bd-start-card i{flex:none;display:grid;place-items:center;width:40px;height:40px;border-radius:12px;background:var(--c);color:#fff}\n.bd-start-card i svg{width:20px;height:20px}\n.bd-start-card b{display:block;font:700 15px var(--bd-serif)}\n.bd-start-card small{display:block;font:500 12.5px var(--bd-sans);color:var(--bd-muted)}\n.bd-skel{display:block;border-radius:16px;background:linear-gradient(90deg,var(--bd-surface-2),var(--bd-line),var(--bd-surface-2));background-size:200% 100%;animation:bd-skel 1.4s ease-in-out infinite}\n.bd-lead.bd-skel{aspect-ratio:16/13}.bd-second .bd-skel{aspect-ratio:16/14}.bd-sec-grid .bd-skel{aspect-ratio:16/14}\n@keyframes bd-skel{to{background-position:-200% 0}}\n.home .bd-sections{margin-top:6px}\n.home #content-wrapper{margin-top:30px!important}\n\n/* Search page */\nhtml.bd-searchpage-ready #content-wrapper{display:none!important}\n.bd-sr{max-width:860px;margin:30px auto 0;padding:0 15px;display:grid;gap:22px}\n.bd-sr *{box-sizing:border-box}\n.bd-sr-head{display:grid;gap:14px;padding-bottom:20px;border-bottom:2px solid var(--bd-ink)}\n.bd-sr-head small{font:700 12px var(--bd-sans);letter-spacing:.14em;text-transform:uppercase;color:var(--bd-accent)}\n.bd-sr-form{display:flex;gap:8px;padding:6px;border-radius:999px;background:var(--bd-surface);border:1px solid var(--bd-line);box-shadow:var(--bd-shadow)}\n.bd-sr-form:focus-within{border-color:var(--bd-accent)}\n.bd-sr-form input{flex:1;min-width:0;border:0;outline:0;background:transparent;color:var(--bd-ink);font:600 clamp(18px,2.6vw,24px) var(--bd-serif);padding:0 16px}\n.bd-sr-form button{border:0;border-radius:999px;background:var(--bd-accent);color:#fff;font:700 15px var(--bd-sans);padding:14px 24px;cursor:pointer}\n.bd-sr-count{margin:0;font:500 15px var(--bd-sans);color:var(--bd-muted)}\n.bd-sr-count b{color:var(--bd-ink)}\n.bd-sr-chips{display:flex;flex-wrap:wrap;gap:8px}\n.bd-chip{display:inline-flex;align-items:center;gap:6px;padding:7px 13px;border-radius:999px;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink)!important;\n  font:600 13.5px var(--bd-sans);text-decoration:none!important;cursor:pointer}\n.bd-chip i{font-style:normal;color:var(--bd-muted);font-weight:500}\n.bd-chip:hover{border-color:var(--bd-accent)}\n.bd-chip.bd-on{background:var(--bd-ink);border-color:var(--bd-ink);color:var(--bd-bg)!important}\n.bd-chip.bd-on i{color:inherit;opacity:.7}\n.bd-sr-list{display:grid}\n.bd-sr-sub{margin:0 0 4px;font:800 22px var(--bd-serif);color:var(--bd-ink)}\n.bd-sr-also{margin:28px 0 0!important;font-size:17px!important;font-family:var(--bd-sans)!important;color:var(--bd-muted)!important}\n.bd-sr-row{display:grid;grid-template-columns:148px minmax(0,1fr);gap:18px;align-items:start;padding:18px 0;border-bottom:1px solid var(--bd-line);text-decoration:none!important;color:var(--bd-ink)!important}\n.bd-sr-img{display:block;aspect-ratio:4/3;border-radius:12px;overflow:hidden;background:var(--bd-surface-2)}\n.bd-sr-img img{width:100%;height:100%;object-fit:cover}\n.bd-sr-text small{display:block;font:700 11.5px var(--bd-sans);letter-spacing:.08em;text-transform:uppercase;color:var(--bd-accent)}\n.bd-sr-text b{display:block;margin-top:4px;font:700 21px/1.3 var(--bd-serif);letter-spacing:-.01em;text-wrap:balance}\n.bd-sr-row:hover b{color:var(--bd-accent)}\n.bd-sr-sum{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;margin-top:6px;font:400 15px/1.55 var(--bd-sans);color:var(--bd-muted)}\n.bd-sr mark{background:color-mix(in srgb,var(--bd-gold) 40%,transparent);color:inherit;border-radius:3px;padding:0 1px}\n.bd-sr-more{justify-self:center;padding:12px 22px;border-radius:999px;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink);font:700 14.5px var(--bd-sans);cursor:pointer}\n.bd-sr-more:hover{border-color:var(--bd-accent);color:var(--bd-accent)}\n.bd-sr-more[hidden],.bd-sr-empty[hidden]{display:none}\n.bd-sr-empty h2{margin:0 0 10px;font:800 28px var(--bd-serif);color:var(--bd-ink)}\n.bd-sr-empty ul{margin:0 0 22px;padding-left:1.2em;font:400 16px/1.7 var(--bd-sans);color:var(--bd-muted)}\n.bd-sr-empty h3{margin:22px 0 12px;font:800 20px var(--bd-serif);color:var(--bd-ink)}\n\n/* Topic (section) pages */\nhtml.bd-topicpage-ready #content-wrapper,html.bd-topicpage .bd-topic-head{display:none!important}\n.bd-tp{max-width:1170px;margin:26px auto 0;padding:0 15px;display:grid;gap:30px}\n.bd-tp *{box-sizing:border-box}\n.bd-tp-head{position:relative;padding:8px 0 24px;border-bottom:2px solid var(--bd-ink)}\n.bd-tp-head small{font:700 12px var(--bd-sans);letter-spacing:.14em;text-transform:uppercase;color:var(--c)}\n.bd-tp-head h1{margin:6px 0 8px;font:800 clamp(38px,6vw,68px)/1 var(--bd-serif);letter-spacing:-.035em;color:var(--bd-ink)}\n.bd-tp-head p{margin:0;font:400 19px/1.5 var(--bd-sans);color:var(--bd-muted);max-width:60ch}\n.bd-tp-meta{display:flex;align-items:center;gap:16px;flex-wrap:wrap;margin-top:16px}\n.bd-tp-count{font:600 14px var(--bd-sans);color:var(--bd-ink)}\n.bd-tp-hub{padding:9px 16px;border-radius:999px;background:var(--c);color:#fff!important;font:700 14px var(--bd-sans);text-decoration:none!important}\n.bd-tp-lead{display:grid;grid-template-columns:minmax(0,2fr) minmax(0,1fr);gap:28px;align-items:start}\n.bd-tp-lead[hidden]{display:none}\n.bd-tp .bd-kick{color:var(--c)}\n.bd-tp-bar{display:flex;align-items:end;justify-content:space-between;gap:16px;flex-wrap:wrap;border-top:1px solid var(--bd-line);padding-top:22px}\n.bd-tp-bar h2{margin:0;font:800 28px var(--bd-serif);letter-spacing:-.02em;color:var(--bd-ink)}\n.bd-tp-filter{width:280px;max-width:100%;height:42px;padding:0 16px;border-radius:999px;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink);font:500 15px var(--bd-sans)}\n.bd-tp-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:28px 24px;margin-top:-6px}\n.bd-tp-card{display:flex;flex-direction:column;gap:8px;text-decoration:none!important;color:var(--bd-ink)!important}\n.bd-tp-card[hidden],.bd-tp:not(.bd-tp-filtering) .bd-tp-dup{display:none}\n.bd-tp-card b{font:700 19px/1.3 var(--bd-serif);letter-spacing:-.01em;text-wrap:balance}\n.bd-tp-card small{font:600 12.5px var(--bd-sans);color:var(--bd-muted)}\n.bd-tp-card:hover b{color:var(--c)}\n.bd-tp-card:hover .bd-sec-img img{transform:scale(1.05)}\n.bd-tp-none{margin:0;font:500 16px var(--bd-sans);color:var(--bd-muted)}\n.bd-tp-more{border-top:2px solid var(--bd-ink);padding-top:18px}\n.bd-tp-more h2{margin:0 0 12px;font:800 22px var(--bd-serif);color:var(--bd-ink)}\n.bd-tp-more div{display:flex;flex-wrap:wrap;gap:8px}\n.bd-tp-more a{padding:8px 14px;border-radius:999px;border:1px solid var(--bd-line);background:var(--bd-surface);color:var(--bd-ink)!important;font:600 14px var(--bd-sans);text-decoration:none!important}\n.bd-tp-more a:hover{border-color:var(--bd-accent);color:var(--bd-accent)!important}\n\n/* Homepage topic sections */\n.bd-sections{max-width:1170px;margin:20px auto 0;padding:0 15px;display:grid;gap:44px}\n.bd-section .bd-section-head{border-top:2px solid var(--bd-ink);padding-top:16px}\n.bd-sec-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:22px}\n.bd-sec-card{display:flex;flex-direction:column;gap:8px;text-decoration:none!important;color:var(--bd-ink)!important}\n.bd-sec-img{display:block;aspect-ratio:16/10;overflow:hidden;border-radius:14px;background:var(--bd-surface-2);margin-bottom:4px}\n.bd-sec-img img{width:100%;height:100%;object-fit:cover;transition:transform .7s var(--bd-ease)}\n.bd-sec-card:hover .bd-sec-img img{transform:scale(1.05)}\n.bd-sec-card small{font:700 11.5px var(--bd-sans);letter-spacing:.1em;text-transform:uppercase;color:var(--bd-accent)}\n.bd-sec-card b{font:700 19px/1.3 var(--bd-serif);letter-spacing:-.01em}\n.bd-sec-card:hover b{color:var(--bd-accent)}\n.bd-stats{display:none!important}\n\n/* Responsive */\n@media (max-width:980px){\n  .bd-hero{margin:18px 12px 6px!important;padding:44px 28px}\n  .bd-hero-grid{grid-template-columns:1fr;gap:32px}\n  .bd-topic-grid{grid-template-columns:repeat(2,1fr)}\n  .bd-lead-grid{grid-template-columns:1fr 1fr}\n  .bd-tp-lead{grid-template-columns:1fr}\n  .bd-tp-lead .bd-second{grid-template-columns:1fr 1fr}\n  .bd-tp-grid{grid-template-columns:repeat(2,minmax(0,1fr))}\n  .bd-lead{grid-column:1/-1}\n  .bd-latest{border-left:0;padding-left:0}\n  .bd-start{grid-template-columns:1fr 1fr 1fr}\n  .bd-start-label{grid-column:1/-1}\n  .bd-sec-grid{grid-template-columns:repeat(2,1fr)}\n  #main-menu{display:none!important}\n  .header-header .container{padding:0 16px!important;box-sizing:border-box!important;max-width:100%!important}\n  .header-menu{order:2;flex-direction:row-reverse;border:0!important}\n  .slide-menu-toggle{display:grid!important;place-items:center;position:static!important;width:40px!important;height:40px!important;line-height:40px!important;margin-left:8px;\n    border-radius:50%;border:1px solid var(--bd-line);background:var(--bd-surface)!important;color:var(--bd-ink)!important;font-size:16px!important}\n  .slide-menu-toggle:before,.slide-menu-toggle:after{color:var(--bd-ink)!important;background:transparent!important}\n  .bd-tools{margin-left:0}\n  .mobile-menu{display:block!important;position:absolute!important;top:52px!important;left:auto!important;right:0;width:min(300px,calc(100vw - 32px))!important;padding:10px!important;\n    border-radius:18px;background:var(--bd-surface)!important;border:1px solid var(--bd-line)!important;box-shadow:var(--bd-shadow-lg);z-index:1000}\n  .mobile-menu #main-menu-nav{display:block!important}\n  .mobile-menu ul li{line-height:44px!important;float:none!important}\n  .mobile-menu ul>li>a{display:block;padding:0 14px!important;line-height:44px!important}\n  .mobile-menu ul>li>a:hover,.mobile-menu ul>li.bd-active>a{background:var(--bd-accent-soft)!important;color:var(--bd-accent)!important}\n  .bd-closer-inner{padding:30px 26px}\n}\n@media (max-width:640px){\n  .bd-hero{padding:36px 22px;border-radius:24px}\n  .bd-hero h1{font-size:40px}\n  .bd-hero p.bd-lead{font-size:16px}\n  .bd-search input{padding:0 10px;font-size:15px}\n  .bd-search button{padding:12px 16px}\n  .bd-stats{gap:20px}\n  .bd-guides{padding:12px}\n  .bd-guide{padding:12px;gap:12px}\n  .bd-guide i{width:44px;height:44px;border-radius:13px}\n  .bd-guide b{font-size:16px}\n  .grid-posts{grid-template-columns:1fr}\n  .bd-topic-grid{grid-template-columns:1fr 1fr;gap:10px}\n  .bd-topic{padding:14px;gap:10px;flex-direction:column;align-items:flex-start}\n  .bd-topic span{display:none}\n  .bd-section-head h2{font-size:26px}\n  .item-post{padding:22px 18px!important;border-radius:18px}\n  .post-body .seo-post{font-size:17px!important}\n  .post-body .seo-related ul{grid-template-columns:1fr}\n  .header-image-wrapper::before{width:38px;height:38px;font-size:22px}\n  .header-image-wrapper::after{font-size:21px}\n  .bd-icon-btn,.slide-menu-toggle{width:38px!important;height:38px!important}\n  .bd-tools{gap:6px}\n  #break-section .widget-title h3.title{padding:0 10px!important}\n  .bd-closer h2{font-size:24px}\n  html.bd-article .item-post{padding:18px 0 0!important}\n  html.bd-article .post-body .seo-toc ol{columns:1}\n  .post-nav{grid-template-columns:1fr!important}\n  .post-nav a,.post-nav .post-next a{text-align:left!important}\n  .bd-pubnote .bd-mark{width:40px;height:40px;font-size:22px}\n  html.bd-article .post-body .seo-post{font-size:18.5px!important;line-height:1.68!important}\n  html.bd-article .post-body h2{font-size:26px!important}\n  .bd-sec-grid{grid-template-columns:1fr 1fr;gap:16px}\n  .bd-lead-grid{grid-template-columns:1fr;gap:22px}\n  .bd-tp-grid{grid-template-columns:1fr 1fr;gap:22px 14px}\n  .bd-sr-row{grid-template-columns:96px minmax(0,1fr);gap:14px;padding:14px 0}\n  .bd-sr-head .bd-sr-chips{flex-wrap:nowrap;overflow-x:auto;scrollbar-width:none;margin:0 -16px;padding:0 16px}\n  .bd-sr-head .bd-sr-chips::-webkit-scrollbar{display:none}\n  .bd-sr-head .bd-chip{flex:none}\n  .bd-sr-text b{font-size:17.5px}\n  .bd-sr-sum{font-size:14px}\n  .bd-sr-form button{padding:12px 16px}\n  .bd-tp-card b{font-size:16px}\n  .bd-tp-filter{width:100%}\n  .bd-tp-head p{font-size:17px}\n  .bd-second{grid-template-columns:1fr 1fr;gap:14px}\n  .bd-story b{font-size:16.5px}\n  .bd-tagline{display:none}\n  .bd-mast-search{flex:1}.bd-mast-search input{width:100%}\n  .bd-start{grid-template-columns:1fr}\n  .bd-sec-card b{font-size:16.5px}\n  .bd-readbar{width:auto;left:12px;right:76px;transform:translateY(140%);bottom:14px}\n  .bd-readbar.bd-show{transform:none}\n  .bd-readbar .bd-rb-toc span{display:none}\n  .bd-readbar .bd-rb-toc{padding:0 12px}\n  .bd-toc-panel{left:12px;right:12px;width:auto;transform:translateY(12px);bottom:70px}\n  .bd-toc-panel.bd-open{transform:none}\n  .bd-upnext{left:12px;right:12px;width:auto;bottom:72px}\n  .bd-404-grid{grid-template-columns:1fr}\n  .bd-topic-head{padding:22px 20px}\n  .bd-resume em{display:none}\n}\n@media (max-width:380px){.header-image-wrapper::after{font-size:18px}}\n@media (prefers-reduced-motion:reduce){\n  *,*::before,*::after{animation:none!important;transition:none!important}\n  .bd-js .bd-reveal{opacity:1;transform:none}\n}\n";
  // The first version of this layer, if still in the theme, already loads Fraunces; reuse it instead of a second copy.
  if (!document.querySelector('link[href*="family=Fraunces:opsz"]')) document.head.appendChild(f);
  document.head.appendChild(s);
  (function () {
  var root = document.documentElement;
  root.classList.add('bd-js');
  try {
    var saved = localStorage.getItem('bd-theme');
    if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) root.classList.add('bd-dark');
  } catch (e) {}

  var BLOG = 'https://randomthoughtsever.blogspot.com/';

  // An older gadget may still load this layer from jsDelivr's branch URL, which can serve a version days old that
  // does not know to stand aside. Stop that script before it runs and hide its empty gadget box.
  if ('MutationObserver' in window && !window.bdStale) {
    window.bdStale = new MutationObserver(function (muts) {
      muts.forEach(function (m) {
        Array.prototype.forEach.call(m.addedNodes, function (n) {
          if (n.tagName === 'SCRIPT' && /My_Repo@[^/]*[^0-9a-f/][^/]*\/seo\/theme\/bd-redesign\.js/.test(n.src || '')) {
            n.type = 'javascript/blocked';
            var w = n.closest && n.closest('.widget'); if (w) w.style.display = 'none';
            n.remove();
          }
        });
      });
    });
    window.bdStale.observe(document.documentElement, { childList: true, subtree: true });
    document.addEventListener('DOMContentLoaded', function () { setTimeout(function () { window.bdStale.disconnect(); }, 0); });
  }
  // Speed: the theme's five random-posts widgets each block the page on six sequential feed requests for full post
  // text (~30 per page). Here the post count comes from a cache, each post becomes an empty slot, and the slots are
  // filled from the small summary feed in parallel once the page is up. This works because the gadget loading this
  // script sits above those widgets.
  var COUNT_KEY = 'bd-post-count';
  function cachedCount() {
    var n = +(window.total_randomposts || 0);
    if (!n) try { var c = JSON.parse(localStorage.getItem(COUNT_KEY) || 'null'); if (c && Date.now() - c.t < 864e5) n = c.n; } catch (e) {}
    return n;
  }
  if (!document.bdWrite) {
    document.bdWrite = document.write;
    document.write = function (h) {
      if (arguments.length === 1 && typeof h === 'string' && h.indexOf('alt=json-in-script') > -1) {
        var n = cachedCount(), k = h.match(/start-index=(\d+)&max-results=1&callback=random_posts\b/);
        if (/callback=randomposts\b/.test(h) && n) return document.bdWrite('<script>randomposts({feed:{openSearch$totalResults:{$t:' + n + '}}});<\/script>');
        if (k) return document.bdWrite('<li class="bd-rp-slot" data-start="' + k[1] + '"></li>');
        h = h.replace('/feeds/posts/default?', '/feeds/posts/summary?');
      }
      return document.bdWrite.apply(document, arguments);
    };
  }
  function fillRandomPosts() {
    var slots = document.querySelectorAll('li.bd-rp-slot');
    Array.prototype.forEach.call(slots, function (li) {
      li.className = '';
      var x = new XMLHttpRequest();
      x.open('GET', '/feeds/posts/summary?alt=json&max-results=1&start-index=' + li.getAttribute('data-start'));
      x.onload = function () {
        try {
          var f = JSON.parse(x.responseText).feed, e = (f.entry || [])[0];
          try { localStorage.setItem(COUNT_KEY, JSON.stringify({ n: +f.openSearch$totalResults.$t, t: Date.now() })); } catch (err) {}
          if (!e) { li.remove(); return; }
          var url = ((e.link || []).filter(function (l) { return l.rel === 'alternate'; })[0] || {}).href, t = e.title.$t, d = e.published.$t;
          var thumb = e.media$thumbnail ? e.media$thumbnail.url : 'https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhbLOy6UDG3zz-MDPFqP76pZpzIwOSIbtahfHcaDBurG4ngL3GAVsdDwe6wofQnSSjuLNR6x2p3X5Rop6O8lhTn988Y6sMPzU2UBNpc1Gnufxf1t5UGj1Qw2UWnCY8nHxS9Y83Dqv1zjJY/s1600/no_thumb.png';
          li.innerHTML = '<a href="' + esc(url) + '" rel="nofollow"><img alt="' + esc(t) + '" loading="lazy" decoding="async" src="' + esc(thumb) + '"/></a><div><a href="' + esc(url) + '" rel="nofollow">' + esc(t) + '</a></div>' +
            '<span><div class="random-info">' + d.substring(8, 10) + '.' + d.substring(5, 7) + '.' + d.substring(0, 4) + '</div></span>';
        } catch (err) { li.remove(); }
      };
      x.onerror = function () { li.remove(); };
      x.send();
    });
  }

  // Speed: the theme downloads the whole newer/older article pages in the background, and parsing them loads
  // every image in them (~25 images, ~600 KB) that is never shown. Hand it those pages with image sources disabled.
  if (!XMLHttpRequest.prototype.bdOpen) {
    var RT = Object.getOwnPropertyDescriptor(XMLHttpRequest.prototype, 'responseText');
    XMLHttpRequest.prototype.bdOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function (m, u) {
      var url = String(u);
      if (RT && RT.get && /^(https?:\/\/[^/]*blogspot\.com)?\/\d{4}\/\d{2}\/[^?#]+\.html/.test(url) && (url.charAt(0) === '/' || url.indexOf(location.host) > -1)) {
        Object.defineProperty(this, 'responseText', { configurable: true, get: function () {
          var t = RT.get.call(this);
          return t && t.replace(/<img\b[^>]*>/gi, function (tag) { return tag.replace(/\s(src|srcset)=/gi, ' data-bd-$1='); });
        } });
      }
      return XMLHttpRequest.prototype.bdOpen.apply(this, arguments);
    };
  }
  // Removed integrations: Facebook SDK, OneSignal push notifications and Pinterest (Facebook and Twitter share
  // buttons stay: they are plain links and need none of these scripts). The theme still
  // references them, so stop them here: the Facebook loader skips itself when its id exists, OneSignal's queued
  // init is cleared, and any script, stylesheet or frame they try to add is dropped.
  if (!document.getElementById('facebook-jssdk')) {
    var fbStub = document.createElement('meta'); fbStub.id = 'facebook-jssdk'; document.head.appendChild(fbStub);
  }
  try { if (Array.isArray(window.OneSignal)) window.OneSignal.length = 0; } catch (e) {}
  var BLOCKED = /connect\.facebook\.net|onesignal\.com|\.os\.tc\b|platform\.twitter\.com|assets\.pinterest\.com/i;
  ['appendChild', 'insertBefore'].forEach(function (m) {
    var orig = Node.prototype[m];
    if (orig.bdWrapped) return;
    Node.prototype[m] = function (n) {
      if (n && /^(SCRIPT|LINK|IFRAME)$/.test(n.tagName || '') && BLOCKED.test(n.src || n.href || '')) return n;
      return orig.apply(this, arguments);
    };
    Node.prototype[m].bdWrapped = true;
  });
  (function () {  // connect to the image server early
    var l = document.createElement('link'); l.rel = 'preconnect'; l.href = 'https://blogger.googleusercontent.com'; document.head.appendChild(l);
  })();
  // Icons are 24x24 stroke paths.
  var P = {
    money: '<path d="M3 7h18v10H3z"/><circle cx="12" cy="12" r="2.5"/><path d="M6 10v4M18 10v4"/>',
    chart: '<path d="M3 20h18"/><path d="m4 15 5-5 4 4 7-7"/><path d="M15 7h5v5"/>',
    brain: '<path d="M9 4a3 3 0 0 0-3 3 3 3 0 0 0-2 5 3 3 0 0 0 2 5 3 3 0 0 0 6 1V5a3 3 0 0 0-3-1z"/><path d="M15 4a3 3 0 0 1 3 3 3 3 0 0 1 2 5 3 3 0 0 1-2 5 3 3 0 0 1-6 1"/>',
    rise: '<path d="M12 20V8"/><path d="m6 13 6-6 6 6"/><path d="M5 4h14"/>',
    heart: '<path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z"/>',
    case: '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18"/>',
    leaf: '<path d="M5 19c0-8 5-14 15-14 0 10-6 15-14 15"/><path d="M5 19c3-4 6-6 9-8"/>',
    chip: '<rect x="6" y="6" width="12" height="12" rx="2"/><path d="M9 2v4M15 2v4M9 18v4M15 18v4M2 9h4M2 15h4M18 9h4M18 15h4"/>',
    flag: '<path d="M5 21V4"/><path d="M5 4h11l-2 4 2 4H5"/>',
    book: '<path d="M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2z"/><path d="M4 19V5"/>',
    globe: '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/>',
    coin: '<circle cx="12" cy="12" r="9"/><path d="M9 9h4.5a1.5 1.5 0 0 1 0 3H9h5a1.5 1.5 0 0 1 0 3H9M11 7v2M11 15v2"/>',
    spark: '<path d="M12 3v4M12 17v4M3 12h4M17 12h4M6 6l2.5 2.5M15.5 15.5 18 18M6 18l2.5-2.5M15.5 8.5 18 6"/>'
  };
  function svg(name) {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + (P[name] || P.spark) + '</svg>';
  }
  var TOPICS = [
    ['Personal Finance', 'Budgeting, saving, FIRE and debt', '#e2462c', 'money'],
    ['Investing', 'Stocks, funds, gold and markets', '#0f9d76', 'chart'],
    ['Psychology', 'How the mind really works', '#6b5cff', 'brain'],
    ['Self Improvement', 'Habits, focus and confidence', '#e48b12', 'rise'],
    ['Love & Relationships', 'Love, breakups and connection', '#e5487d', 'heart'],
    ['Career & Workplace', 'Office life and growing at work', '#2f7de1', 'case'],
    ['Health & Wellness', 'Sleep, stress and healthy living', '#16a34a', 'leaf'],
    ['Technology & AI', 'AI, data and the internet', '#0e9bb5', 'chip']
  ];
  var COLORS = { 'start here': ['#e2462c', 'flag'], 'economics': ['#7c5cff', 'chart'], 'leadership': ['#2f7de1', 'case'], 'education': ['#d97706', 'book'],
    'zen & mindfulness': ['#0f9d76', 'leaf'], 'cryptocurrency': ['#f59e0b', 'coin'], 'countries & living abroad': ['#0e9bb5', 'globe'],
    'politics': ['#64748b', 'flag'], 'parenting & family': ['#e5487d', 'heart'], 'calculators': ['#0f9d76', 'chart'] };
  TOPICS.forEach(function (t) { COLORS[t[0].toLowerCase()] = [t[2], t[3]]; });
  var GUIDES = [
    ['Money', 'Budget, invest and get out of debt', '#e2462c', 'money', BLOG + '2026/10/personal-finance-for-beginners-step-by.html', /money|financ/i],
    ['Mind', 'Calmer, more focused and happier', '#6b5cff', 'brain', BLOG + '2026/10/mental-well-being-and-self-improvement.html', /mind|mental|well/i],
    ['Career', 'Work smarter and avoid burnout', '#2f7de1', 'case', BLOG + '2026/10/career-guide-work-smarter-avoid-burnout.html', /career/i]
  ];
  var TOPIC_ABOUT = { 'Economics': 'How economies, money and markets work', 'Leadership': 'Leading people and teams well', 'Education': 'Learning, study and schools',
    'Zen & Mindfulness': 'Calm, presence and Zen wisdom', 'Cryptocurrency': 'Bitcoin, altcoins and the risks', 'Countries & Living Abroad': 'Life, money and work around the world',
    'Politics': 'How governments and elections work', 'Parenting & Family': 'Raising children and family life' };
  var HUB_LABELS = { 'Personal Finance': 'Money', 'Investing': 'Money', 'Economics': 'Money', 'Career & Workplace': 'Career', 'Leadership': 'Career',
    'Education': 'Career', 'Self Improvement': 'Mind', 'Psychology': 'Mind', 'Health & Wellness': 'Mind', 'Zen & Mindfulness': 'Mind', 'Love & Relationships': 'Mind' };
  var ICONS = {
    moon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/></svg>',
    sun: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>',
    search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>',
    up: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19V5M5 12l7-7 7 7"/></svg>'
  };

  // Animated spot illustrations (600x240 SVG). Shapes use var(--c), the post's topic colour; animation classes are
  // defined in the stylesheet and only run while the figure is on screen.
  var S = function (inner) {
    return '<svg viewBox="0 0 600 240" xmlns="http://www.w3.org/2000/svg"><rect class="ill-bg" width="600" height="240" rx="22"/>' +
      '<circle class="ill-soft a-float" cx="90" cy="60" r="46"/><circle class="ill-soft a-float d2" cx="520" cy="190" r="60"/>' + inner + '</svg>';
  };
  var ILL = {
    coins: function () {
      var c = ''; for (var i = 0; i < 5; i++) c += '<g class="a-drop" style="animation-delay:' + (i * .35) + 's"><ellipse class="ill-main" cx="300" cy="' + (178 - i * 22) + '" rx="62" ry="16"/><ellipse class="ill-light" cx="300" cy="' + (172 - i * 22) + '" rx="62" ry="16"/></g>';
      return S(c + '<g class="a-float"><circle class="ill-light" cx="430" cy="92" r="38"/><text class="ill-sym small big" x="430" y="106" text-anchor="middle">₹</text></g>');
    },
    chart: function () {
      var b = ''; [70, 105, 90, 140, 170].forEach(function (h, i) { b += '<rect class="ill-main a-rise" style="animation-delay:' + (i * .15) + 's" x="' + (170 + i * 56) + '" y="' + (200 - h) + '" width="34" height="' + h + '" rx="6"/>'; });
      return S('<line class="ill-axis" x1="150" y1="200" x2="460" y2="200"/>' + b + '<polyline class="ill-line a-draw" points="175,140 232,108 290,122 346,76 402,46 452,30"/><circle class="ill-dot a-pulse" cx="452" cy="30" r="8"/>');
    },
    shield: function () {
      return S('<circle class="ill-ring a-ping" cx="300" cy="120" r="70"/><path class="ill-main a-pulse" d="M300 40 L370 66 V118 C370 162 338 190 300 204 C262 190 230 162 230 118 V66 Z"/>' +
        '<path class="ill-tick a-draw" d="M268 120 L292 144 L336 98"/>');
    },
    clock: function () {
      return S('<circle class="ill-light" cx="300" cy="120" r="84"/><circle class="ill-face" cx="300" cy="120" r="72"/>' +
        '<g class="ill-ticks">' + [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11].map(function (i) { var a = i * Math.PI / 6; return '<line x1="' + (300 + 60 * Math.sin(a)).toFixed(1) + '" y1="' + (120 - 60 * Math.cos(a)).toFixed(1) + '" x2="' + (300 + 66 * Math.sin(a)).toFixed(1) + '" y2="' + (120 - 66 * Math.cos(a)).toFixed(1) + '"/>'; }).join('') + '</g>' +
        '<line class="ill-hand a-spin slow" x1="300" y1="120" x2="300" y2="76"/><line class="ill-hand thin a-spin" x1="300" y1="120" x2="300" y2="62"/><circle class="ill-dot" cx="300" cy="120" r="7"/>');
    },
    brain: function () {
      var pts = [[230, 90], [280, 60], [340, 70], [380, 110], [350, 160], [290, 175], [240, 145], [300, 115]], l = '';
      [[0, 1], [1, 2], [2, 3], [3, 4], [4, 5], [5, 6], [6, 0], [7, 0], [7, 2], [7, 4], [7, 5], [1, 7], [3, 7]].forEach(function (e, i) {
        l += '<line class="ill-link a-blink" style="animation-delay:' + (i * .2) + 's" x1="' + pts[e[0]][0] + '" y1="' + pts[e[0]][1] + '" x2="' + pts[e[1]][0] + '" y2="' + pts[e[1]][1] + '"/>';
      });
      return S(l + pts.map(function (q, i) { return '<circle class="ill-main a-pulse" style="animation-delay:' + (i * .25) + 's" cx="' + q[0] + '" cy="' + q[1] + '" r="' + (i === 7 ? 14 : 10) + '"/>'; }).join(''));
    },
    heart: function () {
      return S('<path class="ill-main a-beat" d="M300 196 C210 140 200 96 222 70 C246 42 286 50 300 80 C314 50 354 42 378 70 C400 96 390 140 300 196 Z"/>' +
        '<polyline class="ill-line a-draw" points="120,128 200,128 220,96 240,160 262,128 338,128 360,92 380,164 400,128 480,128"/>');
    },
    moon: function () {
      var st = ''; [[180, 60], [230, 150], [420, 70], [460, 150], [380, 190], [150, 170], [500, 50]].forEach(function (q, i) {
        st += '<circle class="ill-star a-blink" style="animation-delay:' + (i * .4) + 's" cx="' + q[0] + '" cy="' + q[1] + '" r="' + (3 + i % 3) + '"/>';
      });
      return S(st + '<g class="a-float"><circle class="ill-main" cx="300" cy="118" r="66"/><circle class="ill-bgc" cx="330" cy="98" r="58"/></g>' +
        '<text class="ill-z a-up" x="372" y="72">z</text><text class="ill-z a-up d2" x="394" y="52">z</text>');
    },
    plant: function () {
      return S('<path class="ill-light" d="M250 200 H350 L338 236 H262 Z"/><path class="ill-stem a-draw" d="M300 200 C300 160 300 130 300 70"/>' +
        '<path class="ill-main a-sway" d="M300 140 C250 140 230 110 232 86 C262 86 296 104 300 140 Z"/><path class="ill-main a-sway d2" d="M300 112 C346 112 366 82 364 58 C334 58 302 76 300 112 Z"/>');
    },
    target: function () {
      return S('<circle class="ill-light" cx="300" cy="120" r="86"/><circle class="ill-face" cx="300" cy="120" r="62"/><circle class="ill-light" cx="300" cy="120" r="40"/><circle class="ill-main a-pulse" cx="300" cy="120" r="18"/>' +
        '<g class="a-shoot"><line class="ill-hand" x1="300" y1="120" x2="420" y2="56"/><path class="ill-main" d="M420 56 l22 -4 -8 20 z"/></g>');
    },
    climb: function () {
      var st = ''; for (var i = 0; i < 5; i++) st += '<rect class="ill-light" x="' + (170 + i * 56) + '" y="' + (200 - i * 30) + '" width="56" height="' + (i * 30 + 14) + '" rx="4"/>';
      return S(st + '<g class="a-climb"><circle class="ill-main" cx="0" cy="-34" r="12"/><rect class="ill-main" x="-10" y="-22" width="20" height="26" rx="8"/></g>' +
        '<path class="ill-main a-pulse" d="M440 40 l8 16 18 3 -13 12 3 18 -16 -9 -16 9 3 -18 -13 -12 18 -3 z"/>');
    },
    globe: function () {
      return S('<circle class="ill-light" cx="300" cy="120" r="84"/><circle class="ill-face" cx="300" cy="120" r="74"/>' +
        '<ellipse class="ill-meridian a-squash" cx="300" cy="120" rx="34" ry="74"/><ellipse class="ill-meridian a-squash d2" cx="300" cy="120" rx="60" ry="74"/>' +
        '<line class="ill-meridian" x1="226" y1="120" x2="374" y2="120"/><ellipse class="ill-meridian" cx="300" cy="84" rx="64" ry="12"/><ellipse class="ill-meridian" cx="300" cy="156" rx="64" ry="12"/>' +
        '<circle class="ill-dot a-orbit" cx="300" cy="120" r="8"/>');
    },
    chip: function () {
      var w = ''; [[170, 80], [170, 120], [170, 160], [430, 80], [430, 120], [430, 160]].forEach(function (q, i) {
        var x2 = q[0] < 300 ? 250 : 350; w += '<line class="ill-link" x1="' + q[0] + '" y1="' + q[1] + '" x2="' + x2 + '" y2="' + q[1] + '"/><circle class="ill-dot a-flow' + (q[0] < 300 ? '' : ' rev') + '" style="animation-delay:' + (i * .3) + 's" cx="' + q[0] + '" cy="' + q[1] + '" r="5"/>';
      });
      return S(w + '<rect class="ill-main" x="250" y="60" width="100" height="120" rx="16"/><rect class="ill-face a-blink" x="272" y="84" width="56" height="72" rx="8"/>');
    },
    bulb: function () {
      var r = ''; for (var i = 0; i < 8; i++) { var a = i * Math.PI / 4; r += '<line class="ill-ray a-blink" style="animation-delay:' + (i * .15) + 's" x1="' + (300 + 78 * Math.cos(a)).toFixed(1) + '" y1="' + (104 + 78 * Math.sin(a)).toFixed(1) + '" x2="' + (300 + 100 * Math.cos(a)).toFixed(1) + '" y2="' + (104 + 100 * Math.sin(a)).toFixed(1) + '"/>'; }
      return S(r + '<circle class="ill-glow a-pulse" cx="300" cy="104" r="62"/><path class="ill-main" d="M300 54 C330 54 352 76 352 104 C352 124 340 136 330 148 V166 H270 V148 C260 136 248 124 248 104 C248 76 270 54 300 54 Z"/>' +
        '<rect class="ill-light" x="272" y="172" width="56" height="14" rx="6"/><rect class="ill-light" x="280" y="190" width="40" height="12" rx="6"/>');
    },
    checklist: function () {
      var r = ''; for (var i = 0; i < 3; i++) {
        r += '<rect class="ill-face" x="200" y="' + (50 + i * 52) + '" width="36" height="36" rx="8"/><rect class="ill-bar" x="252" y="' + (60 + i * 52) + '" width="' + (150 - i * 30) + '" height="16" rx="8"/>' +
          '<path class="ill-tick a-draw" style="animation-delay:' + (i * .6) + 's" d="M208 ' + (68 + i * 52) + ' l9 9 l16 -18"/>';
      }
      return S('<rect class="ill-light" x="180" y="30" width="250" height="186" rx="18"/>' + r);
    },
    scale: function () {
      return S('<rect class="ill-main" x="292" y="60" width="16" height="140" rx="6"/><rect class="ill-main" x="250" y="196" width="100" height="14" rx="7"/>' +
        '<g class="a-tilt"><rect class="ill-main" x="180" y="58" width="240" height="10" rx="5"/><line class="ill-hand thin" x1="200" y1="64" x2="200" y2="120"/><line class="ill-hand thin" x1="400" y1="64" x2="400" y2="120"/>' +
        '<path class="ill-light" d="M160 120 H240 C236 146 164 146 160 120 Z"/><path class="ill-light" d="M360 120 H440 C436 146 364 146 360 120 Z"/></g><circle class="ill-dot" cx="300" cy="62" r="9"/>');
    },
    chat: function () {
      return S('<g class="a-pop"><rect class="ill-main" x="170" y="50" width="180" height="76" rx="22"/><path class="ill-main" d="M210 122 l-14 28 34 -26 z"/></g>' +
        '<g class="a-pop d2"><rect class="ill-light" x="270" y="110" width="170" height="70" rx="22"/><path class="ill-light" d="M404 176 l16 26 -36 -24 z"/></g>' +
        '<circle class="ill-face a-blink" cx="226" cy="88" r="7"/><circle class="ill-face a-blink d2" cx="260" cy="88" r="7"/><circle class="ill-face a-blink d3" cx="294" cy="88" r="7"/>');
    },
    book: function () {
      return S('<path class="ill-main" d="M300 70 C270 54 220 52 180 60 V190 C220 182 270 184 300 200 Z"/><path class="ill-main" d="M300 70 C330 54 380 52 420 60 V190 C380 182 330 184 300 200 Z"/>' +
        '<path class="ill-face a-flip" d="M300 74 C330 60 376 58 410 64 V184 C376 178 330 180 300 194 Z"/>');
    },
    crypto: function () {
      return S('<g class="a-coin"><circle class="ill-main" cx="300" cy="120" r="72"/><circle class="ill-light" cx="300" cy="120" r="56"/><text class="ill-sym" x="300" y="146" text-anchor="middle">₿</text></g>' +
        '<circle class="ill-ring a-ping" cx="300" cy="120" r="72"/>');
    },
    house: function () {
      return S('<path class="ill-main" d="M300 40 L410 120 H384 V204 H216 V120 H190 Z"/><rect class="ill-face" x="282" y="150" width="36" height="54" rx="4"/>' +
        '<rect class="ill-face a-blink" x="236" y="132" width="30" height="28" rx="4"/><rect class="ill-face a-blink d2" x="334" y="132" width="30" height="28" rx="4"/>' +
        '<g class="a-float"><circle class="ill-light" cx="456" cy="70" r="26"/><text class="ill-sym small" x="456" y="80" text-anchor="middle">%</text></g>');
    },
    card: function () {
      return S('<g class="a-swipe"><rect class="ill-main" x="200" y="62" width="200" height="124" rx="16"/><rect class="ill-face" x="200" y="90" width="200" height="22"/>' +
        '<rect class="ill-light" x="220" y="142" width="64" height="12" rx="6"/><rect class="ill-light" x="296" y="142" width="40" height="12" rx="6"/></g>' +
        '<rect class="ill-light" x="160" y="196" width="280" height="18" rx="9"/><rect class="ill-main a-fill" x="160" y="196" width="280" height="18" rx="9"/>');
    }
  };
  // Whole words only, so "rent" does not match "different" or "age" match "manage".
  var MOTIF_WORDS = [
    ['crypto', /\b(crypto\w*|bitcoin|altcoins?|blockchain|ethereum)\b/i],
    ['card', /\b(credit cards?|cibil|credit scores?|debts?|emis?|repay\w*)\b/i],
    ['house', /\b(home loans?|mortgages?|houses?|housing|rent|renting|property|real estate|prepay\w*)\b/i],
    ['moon', /\b(sleep\w*|night|dreams?|insomnia|bedtime)\b/i],
    ['heart', /\b(love|romance|romantic|partners?|marriage|married|dating|breakups?|heart)\b/i],
    ['shield', /\b(insurance|emergenc\w*|protect\w*|safety|safe|risks?|scams?|fraud|security|dangers?)\b/i],
    ['chart', /\b(invest\w*|stocks?|markets?|sip|mutual funds?|returns?|portfolios?|growth|inflation|econom\w*|gdp|interest rates?|nasdaq)\b/i],
    ['coins', /\b(money|savings?|save|budget\w*|salar\w*|pay|income|costs?|prices?|spend\w*|wealth\w*|rich|financ\w*|tax\w*|earn\w*)\b/i],
    ['chip', /\b(ai|artificial|machine learning|technolog\w*|data|internet|google|software|computers?|digital|algorithms?)\b/i],
    ['globe', /\b(country|countries|world|global|abroad|travel\w*|elections?|politic\w*|government\w*|nations?|dubai|china|india|usa|america\w*)\b/i],
    ['climb', /\b(careers?|jobs?|promotions?|leaders?\w*|ceo|coo|managers?|boss\w*|office|workplace|interviews?|resumes?|teams?)\b/i],
    ['book', /\b(books?|education|schools?|study|studying|learn\w*|students?|exams?|reading|summary|lessons?)\b/i],
    ['clock', /\b(time|habits?|routines?|daily|schedules?|morning|retire\w*|aging|ageing|consisten\w*|procrastinat\w*|minutes?|hours?)\b/i],
    ['brain', /\b(mind\w*|psycholog\w*|brain|think\w*|emotions?|emotional|bias\w*|memory|stress\w*|anxiety|overthink\w*|mental)\b/i],
    ['plant', /\b(grow\w*|wellness|zen|mindful\w*|calm|nature|health\w*|meditat\w*|peace\w*|happi\w*|happy)\b/i],
    ['chat', /\b(talk\w*|communicat\w*|social\w*|conversations?|friends?\w*|connect\w*|listen\w*)\b/i],
    ['scale', /\b(vs|versus|compar\w*|differences?|pros|cons|balanc\w*|choos\w*|choices?|better)\b/i],
    ['target', /\b(goals?|plans?|planning|strateg\w*|success\w*|decid\w*|decisions?|focus\w*|achiev\w*)\b/i],
    ['checklist', /\b(steps?|checklists?|how to|tips|mistakes?|ways|rules|guide|signs)\b/i],
    ['bulb', /\b(ideas?|why|what is|understand\w*|explain\w*|myths?|facts?|meaning|truth)\b/i]
  ];
  var TOPIC_MOTIF = { 'personal finance': 'coins', 'investing': 'chart', 'economics': 'chart', 'cryptocurrency': 'crypto', 'psychology': 'brain',
    'self improvement': 'target', 'love & relationships': 'heart', 'career & workplace': 'climb', 'leadership': 'climb', 'health & wellness': 'plant',
    'zen & mindfulness': 'plant', 'technology & ai': 'chip', 'education': 'book', 'countries & living abroad': 'globe', 'politics': 'globe', 'parenting & family': 'heart' };
  var FILLERS = ['bulb', 'checklist', 'target', 'scale', 'clock', 'chat', 'book', 'plant'];
  function title2Text() { return ((document.querySelector('.item-post h1.post-title') || {}).textContent || ''); }
  function wordMotif(text, used) {
    for (var i = 0; i < MOTIF_WORDS.length; i++) if (!used[MOTIF_WORDS[i][0]] && MOTIF_WORDS[i][1].test(text)) return MOTIF_WORDS[i][0];
    return '';
  }
  // The section heading decides first, then the post's topic, then its title.
  function pickMotif(heading, topic, used, k) {
    var m = wordMotif(heading, used);
    if (m) return m;
    var t = TOPIC_MOTIF[(topic || '').toLowerCase()];
    if (t && !used[t]) return t;
    m = wordMotif(title2Text(), used);
    if (m) return m;
    for (var j = 0; j < FILLERS.length; j++) { var f = FILLERS[(j + k) % FILLERS.length]; if (!used[f]) return f; }
    return 'bulb';
  }

  function el(tag, cls, html) { var e = document.createElement(tag); if (cls) e.className = cls; if (html != null) e.innerHTML = html; return e; }
  function esc(s) { return String(s).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; }); }
  function labelUrl(t) { return '/search/label/' + encodeURIComponent(t); }
  function hash(s) { var h = 0; for (var i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0; return Math.abs(h); }

  // Cover art for posts without an image, in place of the theme's grey "no image" picture.
  var PLACEHOLDER = /(\/nth\.png|no[-_]?thumb\.png|no[-_]?image[^/]*)(\?|$)/i;
  var DEEP = ['#12141b', '#1e1b3a', '#2a1530', '#0f2a2a', '#2b1a10'];
  var COVER_ICONS = [['money', /emergency fund/i], ['chart', /calculator|\bsip\b|fire\b|return|invest|stock|mutual fund/i], ['coin', /credit|debt|loan|emi|cibil|crypto|bitcoin/i], ['money', /money|budget|saving|emergency|tax|income|finance/i],
    ['brain', /mind|psycholog|mental|think|stress/i], ['rise', /habit|improve|growth|confiden|success/i], ['heart', /love|relationship|partner/i],
    ['case', /career|job|work|boss|office|leader/i], ['leaf', /health|sleep|calm|zen|well/i], ['chip', /\bai\b|technolog|data|google|internet/i],
    ['globe', /countr|world|global|abroad/i], ['book', /book|learn|study|education|school/i], ['flag', /guide|start here/i]];
  var PALETTE = ['#e2462c', '#0f9d76', '#6b5cff', '#e48b12', '#e5487d', '#2f7de1', '#16a34a', '#0e9bb5'];
  function cover(title, label, big) {
    var key = (label || '').trim().toLowerCase(), c = COLORS[key] || [PALETTE[hash(title || key) % PALETTE.length], 'spark'];
    if (key === 'start here') GUIDES.forEach(function (g) { if (g[5].test(title)) c = [g[2], g[3]]; });
    var W = big ? 800 : 200, H = big ? 500 : 200, letter = esc(((title || label || 'B').trim().charAt(0) || 'B').toUpperCase());
    // Covers in one topic share its colour; a second colour and the icon come from the title, so neighbours differ.
    var hv = hash(title || key), alt = PALETTE[hv % PALETTE.length];
    if (alt === c[0]) alt = PALETTE[(hv + 3) % PALETTE.length];
    for (var ci = 0; ci < COVER_ICONS.length; ci++) if (COVER_ICONS[ci][1].test(title || '')) { c = [c[0], COVER_ICONS[ci][0]]; break; }
    var ang = [[0, 0, 1, 1], [1, 0, 0, 1], [0, 1, 1, 0], [0, 0, 1, .4]][hv % 4];
    var s = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ' + W + ' ' + H + '"><defs><linearGradient id="g" x1="' + ang[0] + '" y1="' + ang[1] + '" x2="' + ang[2] + '" y2="' + ang[3] + '">' +
      '<stop offset="0" stop-color="' + c[0] + '"/><stop offset=".45" stop-color="' + c[0] + '"/><stop offset="1" stop-color="' + alt + '"/></linearGradient>' +
      '<linearGradient id="d" x1="0" y1="0" x2="0" y2="1"><stop offset=".35" stop-color="' + DEEP[hv % DEEP.length] + '" stop-opacity="0"/><stop offset="1" stop-color="' + DEEP[hv % DEEP.length] + '" stop-opacity=".75"/></linearGradient>' +
      '<radialGradient id="r" cx=".85" cy=".15" r=".7"><stop offset="0" stop-color="#fff" stop-opacity=".35"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></radialGradient></defs>' +
      '<rect width="100%" height="100%" fill="url(#g)"/><rect width="100%" height="100%" fill="url(#d)"/><rect width="100%" height="100%" fill="url(#r)"/>';
    if (big) {
      s += '<circle cx="690" cy="430" r="230" fill="none" stroke="#fff" stroke-opacity=".12" stroke-width="2"/>' +
        '<circle cx="690" cy="430" r="160" fill="none" stroke="#fff" stroke-opacity=".12" stroke-width="2"/>' +
        '<g transform="translate(560 300) scale(5.6)" fill="none" stroke="#fff" stroke-opacity=".9" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round">' + P[c[1]] + '</g>' +
        '<text x="56" y="430" font-family="Georgia,serif" font-style="italic" font-weight="700" font-size="300" fill="#fff" fill-opacity=".14">' + letter + '</text>';
    } else {
      s += '<text x="100" y="134" text-anchor="middle" font-family="Georgia,serif" font-style="italic" font-weight="700" font-size="104" fill="#fff" fill-opacity=".92">' + letter + '</text>';
    }
    return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(s + '</svg>');
  }
  function fixImage(img, title, label, big) {
    var src = img.getAttribute('data-src') || img.getAttribute('src') || '';
    if (!src || PLACEHOLDER.test(src)) {
      img.removeAttribute('data-src'); img.removeAttribute('srcset');
      img.src = cover(title, label, big); img.classList.add('bd-cover', 'lazy-yard');
      if (!img.alt) img.alt = '';
      return true;
    }
    return false;
  }


  // Blogger's JSON feed, used for instant search, the 404 page and "up next" thumbnails.
  function feed(q, n, cb, label) {
    var x = new XMLHttpRequest();
    x.open('GET', '/feeds/posts/summary' + (label ? '/-/' + encodeURIComponent(label) : '') + '?alt=json&max-results=' + n + (q ? '&q=' + encodeURIComponent(q) : ''));
    x.onload = function () {
      if (x.status !== 200) { cb([], true); return; }
      try {
        cb((JSON.parse(x.responseText).feed.entry || []).map(function (e) {
          var link = (e.link || []).filter(function (l) { return l.rel === 'alternate'; })[0];
          var cats = (e.category || []).map(function (c) { return c.term; }).filter(function (t) { return t !== 'Start Here'; });
          // Rewritten posts open with an inline <style>, which Blogger's summary keeps as text: drop comments and CSS rules.
          var sum = ((e.summary && e.summary.$t) || '').replace(/<!--[\s\S]*?(-->|$)/g, ' ').replace(/[^{}]*\{[^{}]*(\}|$)/g, ' ')
            .replace(/\s+/g, ' ').replace(/^\s*Updated \d{1,2} \w+ \d{4}\s*/i, '').replace(/^\s*Quick answer:?\s*/i, '').trim();
          if (/[{};]|^[^A-Za-z]/.test(sum) || sum.length < 60) sum = '';
          if (sum.length > 170) sum = sum.slice(0, 165).replace(/\s+\S*$/, '') + '\u2026';
          return { title: e.title.$t, url: link && link.href, label: cats[0] || '', date: (e.published.$t || '').slice(0, 10), thumb: e.media$thumbnail ? e.media$thumbnail.url : '', sum: sum };
        }).filter(function (p) { return p.url; }));
      } catch (err) { cb([], true); }
    };
    x.onerror = function () { cb([], true); };
    x.send();
  }
  function instantSearch(input, box) {
    var timer, seq = 0;
    input.setAttribute('autocomplete', 'off');
    input.addEventListener('input', function () {
      clearTimeout(timer);
      var q = input.value.trim();
      if (q.length < 2) { box.innerHTML = ''; return; }
      timer = setTimeout(function () {
        var mine = ++seq;
        feed(q, 6, function (posts) {
          if (mine !== seq) return;
          box.innerHTML = posts.length ? posts.map(function (p) {
            return '<a href="' + esc(p.url) + '"><img alt="" src="' + cover(p.title, p.label, false) + '"/><span><b>' + esc(p.title) + '</b><small>' + esc(p.label || 'Article') + '</small></span></a>';
          }).join('') : '<div class="bd-empty">No articles match “' + esc(q) + '”. Press Enter to search the whole blog.</div>';
        });
      }, 180);
    });
    input.addEventListener('keydown', function (ev) {
      var links = box.querySelectorAll('a'), cur = box.querySelector('a.bd-sel'), i = Array.prototype.indexOf.call(links, cur);
      if (ev.key === 'ArrowDown' || ev.key === 'ArrowUp') {
        if (!links.length) return;
        ev.preventDefault();
        if (cur) cur.classList.remove('bd-sel');
        i = ev.key === 'ArrowDown' ? (i + 1) % links.length : (i <= 0 ? links.length - 1 : i - 1);
        links[i].classList.add('bd-sel');
      } else if (ev.key === 'Enter' && cur) { ev.preventDefault(); location.href = cur.href; }
    });
  }

  function isHomePath() { return (location.pathname === '/' || location.pathname === '') && !/[?&](q|updated-max|start)=/.test(location.search); }
  function setMeta(attr, key, val) {
    var m = document.head.querySelector('meta[' + attr + '="' + key + '"]');
    if (!m) { m = document.createElement('meta'); m.setAttribute(attr, key); document.head.appendChild(m); }
    m.setAttribute('content', val);
  }
  function longDate(iso) {
    var d = new Date(iso || '');
    return isNaN(d) ? '' : d.getDate() + ' ' + ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'][d.getMonth()] + ' ' + d.getFullYear();
  }

  // Reading history, kept in this browser only.
  function store(key, val) {
    try {
      if (val === undefined) return JSON.parse(localStorage.getItem(key) || 'null');
      localStorage.setItem(key, JSON.stringify(val));
    } catch (e) { return null; }
  }
  function cleanUrl(u) { return String(u).split('#')[0].split('?')[0].replace(/^http:/, 'https:'); }

  function init() {
    fillRandomPosts();
    setMeta('name', 'robots', 'max-image-preview:large');
    // Search snippets for the homepage and topic pages, in place of the blog's old keyword-list description.
    var siteDesc = 'Practical, in-depth guides on personal finance, investing, psychology, relationships and careers, with free calculators.';
    var labelPage = location.pathname.match(/^\/search\/label\/([^/]+)$/);
    if (isHomePath()) { setMeta('name', 'description', siteDesc); setMeta('property', 'og:description', siteDesc); }
    else if (labelPage) {
      var ln = decodeURIComponent(labelPage[1].replace(/\+/g, ' ')), li2 = TOPICS.filter(function (t) { return t[0] === ln; })[0];
      var ld = ln + ' guides on Blog Digest' + (li2 ? ': ' + li2[1].toLowerCase() + '.' : '.') + ' Practical, in-depth and free.';
      setMeta('name', 'description', ld); setMeta('property', 'og:description', ld);
    }
    // Remove what the first version of this layer added, if it is still in the theme.
    document.querySelectorAll('style#bd-redesign').forEach(function (e) { e.remove(); });
    document.querySelectorAll('.bd-hero, .bd-topics, .bd-tools, #bd-top, #bd-progress').forEach(function (e) { e.remove(); });
    document.querySelectorAll('.bd-reveal').forEach(function (e) { e.classList.remove('bd-reveal', 'bd-in'); e.style.transitionDelay = ''; });
    var path = location.pathname, isHome = (path === '/' || path === '') && !location.search.match(/[?&](q|updated-max|start)=/);
    var isPost = /^\/\d{4}\/\d{2}\/.+\.html$/.test(path);

    // Header tools: search + dark mode.
    var menu = document.querySelector('.header-menu');
    var modal = el('div', 'bd-search-modal',
      '<form action="/search" method="get" role="search"><input name="q" type="search" placeholder="Search articles, topics and guides…" aria-label="Search"/>' +
      '<button type="submit">Search</button></form><div class="bd-search-results" role="listbox"></div><p>Press Esc to close</p>');
    modal.setAttribute('aria-hidden', 'true');
    document.body.appendChild(modal);
    var openSearch = function () { modal.classList.add('bd-open'); modal.setAttribute('aria-hidden', 'false'); setTimeout(function () { modal.querySelector('input').focus(); }, 50); };
    var closeSearch = function () { modal.classList.remove('bd-open'); modal.setAttribute('aria-hidden', 'true'); };
    modal.addEventListener('click', function (ev) { if (ev.target === modal) closeSearch(); });
    instantSearch(modal.querySelector('input'), modal.querySelector('.bd-search-results'));
    addEventListener('keydown', function (ev) {
      if (ev.key === 'Escape') closeSearch();
      if (ev.key === '/' && !/INPUT|TEXTAREA/.test((document.activeElement || {}).tagName || '')) { ev.preventDefault(); openSearch(); }
    });
    if (menu && !document.querySelector('.bd-tools')) {
      var tools = el('div', 'bd-tools');
      var s = el('a', 'bd-icon-btn', ICONS.search); s.href = '/search'; s.setAttribute('aria-label', 'Search');
      s.addEventListener('click', function (ev) { ev.preventDefault(); openSearch(); });
      var d = el('button', 'bd-icon-btn'); d.type = 'button'; d.setAttribute('aria-label', 'Toggle dark mode');
      var paint = function () { d.innerHTML = root.classList.contains('bd-dark') ? ICONS.sun : ICONS.moon; };
      paint();
      d.addEventListener('click', function () {
        root.classList.toggle('bd-dark'); paint();
        try { localStorage.setItem('bd-theme', root.classList.contains('bd-dark') ? 'dark' : 'light'); } catch (e) {}
      });
      tools.appendChild(s); tools.appendChild(d); menu.appendChild(tools);
    }
    document.querySelectorAll('#main-menu-nav > li > a').forEach(function (a) {
      if (a.pathname === path) a.parentNode.classList.add('bd-active');
    });

    var header = document.getElementById('header-wrap');

    // Card thumbnails: load a sharp image straight away instead of the theme's 72px placeholder.
    // Card thumbnails: sharp, right-sized WebP instead of the theme's 72px placeholder (an 800px JPEG is ~200 KB, a 640px WebP ~65 KB).
    var sized = function (u, w) {
      var seg = 'w' + w + '-h' + Math.round(w * .625) + '-p-k-no-nu-rw';
      return u.replace(/\/(s\d+[^/]*|w\d+[^/]*)\//, '/' + seg + '/').replace(/=(s\d+[^=/]*|w\d+[^=/]*)$/, '=' + seg);
    };
    var cardNo = 0;
    document.querySelectorAll('.index-post').forEach(function (p) {
      var img = p.querySelector('.post-thumb'), a = p.querySelector('.post-title a'), tag = p.querySelector('.post-tag');
      if (!img) return;
      var first = cardNo++ < 2;
      if (!fixImage(img, a ? a.textContent : '', tag ? tag.textContent : '', true)) {
        var orig = img.getAttribute('data-src') || img.getAttribute('src');
        if (!first) img.loading = 'lazy';
        else img.setAttribute('fetchpriority', 'high');
        img.decoding = 'async';
        img.sizes = '(max-width: 640px) 92vw, (max-width: 980px) 46vw, ' + (first && p.matches('.home .grid-posts .index-post:first-child') ? '420px' : '380px');
        img.srcset = [400, 640, 960].map(function (w) { return sized(orig, w) + ' ' + w + 'w'; }).join(', ');
        img.src = sized(orig, 640);
      }
      img.classList.add('lazy-yard');
    });
    var paintWidgets = function () {
      document.querySelectorAll('img').forEach(function (img) {
        if (img.classList.contains('bd-cover') || img.closest('.post-body, .index-post')) return;
        var a = img.closest('a'), li = img.closest('li, .post, .related-item'), t = (li && (li.querySelector('.post-title') || li.querySelector('div > a, h2, h3'))) || a;
        var tag = li && li.querySelector('.post-tag');
        var wide = img.closest('.related-posts, .FeaturedPost, .related-wrap');
        fixImage(img, (t && t.textContent) || img.alt || '', tag ? tag.textContent : '', !!wide);
      });
      document.querySelectorAll('#random-posts .random-info').forEach(function (r) {
        if (r.dataset.bd) return; r.dataset.bd = 1;
        r.innerHTML = r.innerHTML.replace(/\s*-\s*\d+\s+Comments?(\s+Disabled)?/i, '');
      });
      document.querySelectorAll('.recent-posts-details').forEach(function (r) {
        if (r.dataset.bd) return; r.dataset.bd = 1;
        var when = (r.firstChild && r.firstChild.nodeType === 3 ? r.firstChild.nodeValue : '').replace(/\|/g, '').trim();
        if (when) r.textContent = when;
      });
    };
    paintWidgets();
    // The theme fills its widgets from the feed after load.
    if ('MutationObserver' in window) {
      var pend = 0;
      new MutationObserver(function () { if (!pend) pend = setTimeout(function () { pend = 0; paintWidgets(); }, 120); })
        .observe(document.body, { childList: true, subtree: true });
    }

    // Homepage: a magazine front page. Masthead, section navigation, a lead package (lead story, two secondary
    // stories and a list of the latest headlines), the "Start here" guides, then Money, Mind and Work sections.
    // Stories come from the summary feed, cached in the browser so repeat visits paint at once.
    var content = document.getElementById('content-wrapper');
    if (isHome && content && !document.querySelector('.bd-front')) {
      var today = new Date();
      var dateLine = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][today.getDay()] + ', ' + longDate(today.toISOString());
      var NAV = [['Money', 'Personal Finance'], ['Investing', 'Investing'], ['Mind', 'Psychology'], ['Self Improvement', 'Self Improvement'],
        ['Relationships', 'Love & Relationships'], ['Work', 'Career & Workplace'], ['Health', 'Health & Wellness'], ['Tech', 'Technology & AI']];
      var front = el('section', 'bd-front',
        '<div class="bd-mast"><span class="bd-date">' + esc(dateLine) + '</span><span class="bd-tagline">Practical guides to money, mind and work</span>' +
        '<form class="bd-mast-search" action="/search" method="get" role="search"><input name="q" type="search" placeholder="Search articles" aria-label="Search articles"/></form></div>' +
        '<nav class="bd-secnav" aria-label="Sections">' + NAV.map(function (n) { return '<a href="' + labelUrl(n[1]) + '">' + esc(n[0]) + '</a>'; }).join('') +
        '<a href="' + BLOG + '2021/06/sitemap.html">All articles</a></nav>' +
        '<div class="bd-lead-grid"><div class="bd-lead bd-skel"></div><div class="bd-second"><div class="bd-skel"></div><div class="bd-skel"></div></div>' +
        '<aside class="bd-latest"><h2>Latest</h2><ol class="bd-latest-list"></ol></aside></div>' +
        '<div class="bd-start"><span class="bd-start-label">New here? Start with a guide</span>' + GUIDES.map(function (g) {
          return '<a class="bd-start-card" style="--c:' + g[2] + '" href="' + g[4] + '"><i>' + svg(g[3]) + '</i><span><b>' + g[0] + '</b><small>' + g[1] + '</small></span></a>';
        }).join('') + '</div>');
      content.parentNode.insertBefore(front, content);
      var secs = el('div', 'bd-sections', '');
      var SECS = [['Money', 'Personal Finance', 'Budgeting, investing, loans and getting out of debt.'],
        ['Mind', 'Psychology', 'How the mind works, and how to look after yours.'],
        ['Work', 'Career & Workplace', 'Office life, leadership and growing your career.']];
      SECS.forEach(function (sec) {
        secs.appendChild(el('section', 'bd-section', '<div class="bd-section-head"><div><h2>' + sec[0] + '</h2><p>' + esc(sec[2]) + '</p></div>' +
          '<a href="' + labelUrl(sec[1]) + '">All ' + esc(sec[1].toLowerCase()) + ' →</a></div><div class="bd-sec-grid"><span class="bd-skel"></span><span class="bd-skel"></span><span class="bd-skel"></span><span class="bd-skel"></span></div>'));
      });
      content.parentNode.insertBefore(secs, content);
      var ht = document.querySelector('.home-title span');
      if (ht) ht.textContent = 'More stories';

      var pic = function (p, w) { return p.thumb ? sized(p.thumb, w) : cover(p.title, p.label, true); };
      var metaLine = function (p) { return esc(p.label || 'Blog Digest') + (p.date ? ' · ' + esc(longDate(p.date)) : ''); };
      var renderFront = function (posts) {
        if (!posts || !posts.length) return {};
        posts = posts.filter(function (p) { return !/^quotes?$/i.test(p.label) && !/^quote of the day/i.test(p.title); });
        var used = {}, withPic = posts.filter(function (p) { return p.thumb; });
        var lead = withPic[0] || posts[0]; used[lead.url] = 1;
        var second = withPic.filter(function (p) { return !used[p.url]; }).slice(0, 2); second.forEach(function (p) { used[p.url] = 1; });
        var latest = posts.filter(function (p) { return !used[p.url]; }).slice(0, 6); latest.forEach(function (p) { used[p.url] = 1; });
        var leadEl = front.querySelector('.bd-lead');
        leadEl.className = 'bd-lead';
        leadEl.innerHTML = '<a href="' + esc(lead.url) + '"><span class="bd-lead-img"><img alt="" fetchpriority="high" decoding="async" src="' + esc(pic(lead, 960)) + '" srcset="' +
          (lead.thumb ? [640, 960, 1280].map(function (w) { return esc(sized(lead.thumb, w)) + ' ' + w + 'w'; }).join(', ') : esc(pic(lead, 960)) + ' 800w') + '" sizes="(max-width: 980px) 92vw, 560px"/></span>' +
          '<small class="bd-kick">' + esc(lead.label || 'Featured') + '</small><h1>' + esc(lead.title) + '</h1>' + (lead.sum ? '<p>' + esc(lead.sum) + '</p>' : '') +
          '<span class="bd-meta">' + metaLine(lead) + '</span></a>';
        front.querySelector('.bd-second').innerHTML = second.map(function (p) {
          return '<a class="bd-story" href="' + esc(p.url) + '"><span class="bd-story-img"><img alt="" decoding="async" src="' + esc(pic(p, 640)) + '"/></span>' +
            '<small class="bd-kick">' + esc(p.label || 'Blog Digest') + '</small><b>' + esc(p.title) + '</b></a>';
        }).join('');
        front.querySelector('.bd-latest-list').innerHTML = latest.map(function (p) {
          return '<li><a href="' + esc(p.url) + '"><small>' + metaLine(p) + '</small><b>' + esc(p.title) + '</b></a></li>';
        }).join('');
        return used;
      };
      var renderSections = function (used) {
        Array.prototype.forEach.call(secs.querySelectorAll('.bd-section'), function (box, i) {
          var sec = SECS[i];
          feed('', 16, function (posts) {
            var fresh = posts.filter(function (p) { return !used[p.url]; });
            var pick = fresh.filter(function (p) { return p.thumb; }).concat(fresh.filter(function (p) { return !p.thumb; })).slice(0, 4);
            if (!pick.length) { box.remove(); return; }
            pick.forEach(function (p) { used[p.url] = 1; });
            box.querySelector('.bd-sec-grid').innerHTML = pick.map(function (p) {
              return '<a class="bd-sec-card" href="' + esc(p.url) + '"><span class="bd-sec-img"><img alt="" loading="lazy" decoding="async" src="' + esc(pic(p, 640)) + '"/></span>' +
                '<small>' + esc(p.label || sec[1]) + '</small><b>' + esc(p.title) + '</b></a>';
            }).join('');
          }, sec[1]);
        });
      };
      var cached = store('bd-front'), painted = false;
      if (cached && cached.p && Date.now() - cached.t < 6 * 3600e3) { renderFront(cached.p); painted = true; }
      feed('', 25, function (posts) {
        if (posts.length) store('bd-front', { t: Date.now(), p: posts });
        var used = renderFront(posts.length ? posts : (cached && cached.p) || []);
        if (!painted || posts.length) renderSections(used);
      });
    }
    var footer = document.getElementById('footer-wrapper');
    if (footer && !document.querySelector('.bd-closer')) {
      footer.parentNode.insertBefore(el('section', 'bd-closer',
        '<div class="bd-closer-inner"><div><h2>Find your next great read</h2><p>Every guide on Blog Digest, sorted by topic, plus free calculators for SIPs, EMIs, FIRE and debt payoff.</p></div>' +
        '<a href="' + BLOG + '2021/06/sitemap.html">Browse all articles →</a></div>'), footer);
    }

    // "New" badge on posts from the last 30 days.
    document.querySelectorAll('.index-post').forEach(function (p) {
      var t = p.querySelector('time[datetime], abbr[title], .published');
      var when = t && Date.parse(t.getAttribute('datetime') || t.getAttribute('title') || t.textContent);
      var a = p.querySelector('.post-title a');
      if (a && when && Date.now() - when < 30 * 864e5 && !a.parentNode.querySelector('.bd-new')) a.parentNode.appendChild(el('span', 'bd-new', 'NEW'));
    });

    // Article: reading time, progress bar, updated pill, quick answer, drop cap and FAQ.
    var body = document.querySelector('.item-post .post-body');
    if (isPost && body) {
      var words = (body.innerText || '').split(/\s+/).length, mins = Math.max(1, Math.round(words / 230));
      var meta = document.querySelector('.item-post .post-meta');
      if (meta && !meta.querySelector('.bd-readtime')) meta.appendChild(el('span', 'bd-readtime', mins + ' min read'));
      var bar = el('div'); bar.id = 'bd-progress'; document.body.appendChild(bar);
      var onRead = function () {
        var r = body.getBoundingClientRect(), total = r.height - innerHeight * .6;
        bar.style.width = Math.min(100, Math.max(0, -r.top / total * 100)) + '%';
      };
      addEventListener('scroll', onRead, { passive: true }); onRead();

      var post = body.querySelector('.seo-post') || body, dropped = false;
      Array.prototype.slice.call(post.children, 0, 6).forEach(function (p) {
        if (p.tagName !== 'P') return;
        var txt = p.textContent.trim();
        if (/^Updated\s/i.test(txt) && txt.length < 60) p.classList.add('bd-updated');
        else if (/^Quick answer/i.test(txt)) p.classList.add('bd-quick');
        else if (!dropped && txt.length > 120 && /^[A-Za-z]/.test(txt) && !p.querySelector('img')) { p.classList.add('bd-dropcap'); dropped = true; }
      });
      var inFaq = false;
      post.querySelectorAll('h2, h3').forEach(function (h) {
        if (h.tagName === 'H2') inFaq = /FAQ|Frequently asked/i.test(h.textContent);
        else if (inFaq) h.classList.add('bd-faq-q');
      });
    }



    // Article illustrations: five small animated drawings at evenly spaced section breaks, each chosen from the
    // heading it introduces (falling back to the post's topic). Added at view time; the post itself is unchanged.
    if (isPost && body && !post.querySelector('.bd-ill')) {
      var sectionHeads = Array.prototype.filter.call(post.querySelectorAll('h2'), function (h) {
        if (h.closest('.seo-related, .seo-toc, .bd-calc')) return false;
        if (/FAQ|Frequently asked|Related|Final thoughts|Conclusion|Further reading|Keep reading/i.test(h.textContent)) return false;
        var prev = h.previousElementSibling;
        return !(prev && (prev.matches('.separator, .bd-inline-read, .bd-ill') || prev.querySelector && prev.querySelector('img')));
      });
      var bc = document.querySelectorAll('#breadcrumb a'), postTopic = bc.length > 1 ? bc[bc.length - 1].textContent.trim() : '';
      var picks5 = [], n5 = Math.min(5, sectionHeads.length);
      for (var k = 0; k < n5; k++) picks5.push(sectionHeads[Math.floor((k + 0.5) * sectionHeads.length / n5)]);
      var usedMotifs = {};
      picks5.forEach(function (h, k) {
        var name = pickMotif(h.textContent, postTopic, usedMotifs, k);
        usedMotifs[name] = 1;
        var fig = el('figure', 'bd-ill', ILL[name]());
        fig.setAttribute('aria-hidden', 'true');
        fig.style.setProperty('--c', (COLORS[postTopic.toLowerCase()] || ['#e2462c'])[0]);
        h.parentNode.insertBefore(fig, h);
      });
      if ('IntersectionObserver' in window) {
        var illIO = new IntersectionObserver(function (es) { es.forEach(function (e) { e.target.classList.toggle('bd-ill-on', e.isIntersecting); }); }, { rootMargin: '80px' });
        post.querySelectorAll('.bd-ill').forEach(function (f) { illIO.observe(f); });
      } else post.querySelectorAll('.bd-ill').forEach(function (f) { f.classList.add('bd-ill-on'); });
    }

    // Editorial article page: one reading column, topic kicker, byline, sharp responsive images and a search snippet
    // written from the quick answer instead of the old keyword-list description.
    if (isPost && body) {
      root.classList.add('bd-article');
      if (!document.querySelector('link[href*="family=Newsreader"]')) {
        var nf = document.createElement('link'); nf.rel = 'stylesheet';
        nf.href = 'https://fonts.googleapis.com/css2?family=Newsreader:ital,opsz,wght@0,6..72,400;0,6..72,600;1,6..72,400&display=swap';
        document.head.appendChild(nf);
      }
      var itemPost = document.querySelector('.item-post'), h1 = itemPost && itemPost.querySelector('h1.post-title');
      var crumbs = document.querySelectorAll('#breadcrumb a'), topic = crumbs.length > 1 ? crumbs[crumbs.length - 1].textContent.trim() : '';
      var updP = post.querySelector('p.bd-updated'), pubEl = document.querySelector('.item-post .post-date');
      var when = updP ? 'Updated ' + updP.textContent.replace(/^Updated\s*/i, '').trim() : (pubEl ? longDate(pubEl.getAttribute('datetime')) : '');
      if (h1 && !itemPost.querySelector('.bd-byline')) {
        if (topic && !/^home$/i.test(topic)) { var kick = el('a', 'bd-kicker', esc(topic)); kick.href = labelUrl(topic); h1.parentNode.insertBefore(kick, h1); }
        h1.parentNode.insertBefore(el('div', 'bd-byline', '<span class="bd-mark" aria-hidden="true">B</span><span><b>Blog Digest</b><small>' +
          esc(when) + (when ? ' · ' : '') + mins + ' min read</small></span>'), h1.nextSibling);
      }
      var GUC = /^https:\/\/(blogger\.googleusercontent\.com\/img\/|\d\.bp\.blogspot\.com\/)/;
      var widthOnly = function (u, w) {
        var seg = 'w' + w + '-rw';
        return u.replace(/\/(s\d+[^/]*|w\d+[^/]*)\/([^/]*)$/, '/' + seg + '/$2').replace(/=(s\d+[^=/]*|w\d+[^=/]*)$/, '=' + seg);
      };
      Array.prototype.forEach.call(post.querySelectorAll('img'), function (img, i) {
        var src = img.getAttribute('src') || '';
        if (!GUC.test(src) || img.closest('.bd-calc, .seo-related, .bd-inline-read')) return;
        img.removeAttribute('width'); img.removeAttribute('height');
        img.sizes = '(max-width: 780px) 92vw, 680px';
        img.srcset = [480, 800, 1200].map(function (w) { return widthOnly(src, w) + ' ' + w + 'w'; }).join(', ');
        img.src = widthOnly(src, 800);
        img.decoding = 'async';
        if (i > 0) img.loading = 'lazy';
      });
      var snip = post.querySelector('p.bd-quick') || post.querySelector('p.bd-dropcap');
      if (snip) {
        var st = snip.textContent.replace(/^\s*Quick answer:?\s*/i, '').replace(/\s+/g, ' ').trim();
        if (st.length > 158) st = st.slice(0, 155).replace(/\s+\S*$/, '') + '…';
        setMeta('name', 'description', st); setMeta('property', 'og:description', st);
      }
    }



    // Article furniture: long contents lists fold to five, an end mark closes the text, and a short publication
    // note with the three guides sits before the tags.
    if (isPost && body) {
      Array.prototype.forEach.call(post.querySelectorAll('.seo-toc'), function (toc) {
        var items = toc.querySelectorAll('li');
        if (items.length <= 6 || toc.querySelector('.bd-toc-more')) return;
        toc.classList.add('bd-toc-folded');
        var more = el('button', 'bd-toc-more', 'Show all ' + items.length + ' sections');
        more.type = 'button';
        more.addEventListener('click', function () {
          var folded = toc.classList.toggle('bd-toc-folded');
          more.textContent = folded ? 'Show all ' + items.length + ' sections' : 'Show fewer';
        });
        toc.appendChild(more);
      });
      var endP = Array.prototype.filter.call(post.querySelectorAll(':scope > p'), function (p) {
        var t = p.textContent.trim();
        return t.length > 80 && !p.matches('.bd-quick, .bd-updated') && !(p.children.length === 1 && p.firstElementChild.tagName === 'EM' && p.firstElementChild.textContent.trim() === t);
      }).pop();
      if (endP && !post.querySelector('.bd-end')) endP.appendChild(el('span', 'bd-end'));
      var pfoot = document.querySelector('.item-post .post-footer');
      if (pfoot && !document.querySelector('.bd-pubnote')) {
        pfoot.parentNode.insertBefore(el('aside', 'bd-pubnote', '<span class="bd-mark" aria-hidden="true">B</span><div><b>Blog Digest</b>' +
          '<p>Practical, in-depth guides on money, the mind and work, updated regularly. New here? Start with a guide:</p>' +
          '<span class="bd-pub-links">' + GUIDES.map(function (g) { return '<a href="' + g[4] + '">' + g[0] + '</a>'; }).join('') + '</span></div>'), pfoot);
      }
    }

    // Article: reading bar, contents, "up next", inline related link, history, lazy images, external links.
    if (isPost && body) {
      var post2 = body.querySelector('.seo-post') || body, here = cleanUrl(location.href);
      var heads = Array.prototype.filter.call(post2.querySelectorAll('h2'), function (h) { return !h.closest('.seo-related, .seo-toc, .bd-calc'); });
      var title2 = ((document.querySelector('.item-post h1.post-title') || {}).textContent || document.title).trim();
      var relLinks = Array.prototype.filter.call(body.querySelectorAll('.seo-related li a'), function (a) { return cleanUrl(a.href) !== here; });
      if (!relLinks.length) heads.forEach(function (h) {
        var list = h.nextElementSibling;
        if (/related|read next|further reading|keep reading/i.test(h.textContent) && list && /^(UL|OL)$/.test(list.tagName))
          relLinks = Array.prototype.filter.call(list.querySelectorAll('a'), function (a) { return a.hostname === location.hostname && cleanUrl(a.href) !== here; });
      });
      var navNext = document.querySelector('.post-nav .post-prev a, .post-nav .post-next a');
      var next = relLinks[0] ? { url: relLinks[0].href, title: relLinks[0].textContent.trim() } :
        navNext ? { url: navNext.href, title: navNext.textContent.replace(/^\s*(Older|Newer)\s*/i, '').trim() } : null;

      // A "keep reading" card halfway through long articles.
      var mid = relLinks[1] || relLinks[0];
      if (mid && heads.length >= 4 && !post2.querySelector('.bd-inline-read')) {
        var card = el('a', 'bd-inline-read', '<img alt="" src="' + cover(mid.textContent, '', false) + '"/><span><small>Keep reading</small><b>' + esc(mid.textContent.trim()) + '</b></span>');
        card.href = mid.href;
        heads[Math.floor(heads.length / 2)].parentNode.insertBefore(card, heads[Math.floor(heads.length / 2)]);
      }

      // Reading bar with the current section, a contents panel and the next article.
      if (heads.length >= 3) {
        heads.forEach(function (h, i) { if (!h.id) h.id = 'bd-s' + (i + 1); });
        var panel = el('nav', 'bd-toc-panel', '<p>In this article</p>' + heads.map(function (h, i) {
          return '<a href="#' + h.id + '"><i>' + (i + 1) + '</i><span>' + esc(h.textContent.trim()) + '</span></a>';
        }).join(''));
        panel.setAttribute('aria-label', 'Contents');
        var rb = el('div', 'bd-readbar',
          '<button type="button" class="bd-rb-toc" aria-expanded="false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 6h16M4 12h16M4 18h10"/></svg><span>Contents</span></button>' +
          '<div class="bd-rb-now"></div>' + (next ? '<a class="bd-rb-next" href="' + esc(next.url) + '">Next →</a>' : ''));
        document.body.appendChild(panel); document.body.appendChild(rb);
        // Wide screens: the same contents as a rail beside the text, with the next article at the foot.
        var rail = el('nav', 'bd-rail', '<p>In this article</p>' + heads.map(function (h, i) {
          return '<a href="#' + h.id + '"><span>' + esc(h.textContent.trim()) + '</span></a>';
        }).join('') + (next ? '<a class="bd-rail-next" href="' + esc(next.url) + '"><small>Next article</small>' + esc(next.title) + '</a>' : ''));
        rail.setAttribute('aria-label', 'Contents');
        document.body.appendChild(rail);
        var railLinks = rail.querySelectorAll('a[href^="#"]');
        var tocBtn = rb.querySelector('.bd-rb-toc'), now = rb.querySelector('.bd-rb-now');
        var togglePanel = function (open) { panel.classList.toggle('bd-open', open); tocBtn.setAttribute('aria-expanded', open ? 'true' : 'false'); };
        tocBtn.addEventListener('click', function () { togglePanel(!panel.classList.contains('bd-open')); });
        panel.addEventListener('click', function (ev) { if (ev.target.closest('a')) togglePanel(false); });
        document.addEventListener('click', function (ev) { if (!ev.target.closest('.bd-toc-panel, .bd-readbar')) togglePanel(false); });
        var links2 = panel.querySelectorAll('a'), lastCur = -1;
        var onBar = function () {
          var r = body.getBoundingClientRect(), cur = -1;
          rb.classList.toggle('bd-show', r.top < -300 && r.bottom > innerHeight * .9);
          rail.classList.toggle('bd-show', r.top < -200 && r.bottom > innerHeight * .6);
          if (!rb.classList.contains('bd-show')) togglePanel(false);
          heads.forEach(function (h, i) { if (h.getBoundingClientRect().top < 170) cur = i; });
          if (cur !== lastCur) {
            lastCur = cur;
            links2.forEach(function (a, i) { a.classList.toggle('bd-cur', i === cur); });
            railLinks.forEach(function (a, i) { a.classList.toggle('bd-cur', i === cur); });
            now.innerHTML = cur < 0 ? esc(title2) : '<b>' + (cur + 1) + '/' + heads.length + '</b> · ' + esc(heads[cur].textContent.trim());
          }
        };
        addEventListener('scroll', onBar, { passive: true }); onBar();
      }

      // "Up next" once the reader is well into the article; dismissing it hides it for this visit.
      if (next) {
        var up = el('a', 'bd-upnext', '<img alt="" src="' + cover(next.title, '', false) + '"/><span><small>Up next</small><b>' + esc(next.title) + '</b></span><button type="button" aria-label="Dismiss">×</button>');
        up.href = next.url;
        document.body.appendChild(up);
        up.querySelector('button').addEventListener('click', function (ev) {
          ev.preventDefault(); ev.stopPropagation(); up.classList.remove('bd-show'); up.dataset.off = 1;
          try { sessionStorage.setItem('bd-upnext-off', '1'); } catch (e) {}
        });
        var offNow = false; try { offNow = sessionStorage.getItem('bd-upnext-off') === '1'; } catch (e) {}
        if (offNow) up.dataset.off = 1;
        var onUp = function () {
          var r = body.getBoundingClientRect(), pct = -r.top / Math.max(1, r.height - innerHeight);
          up.classList.toggle('bd-show', !up.dataset.off && pct > .6 && r.bottom > innerHeight * .9);
        };
        addEventListener('scroll', onUp, { passive: true });
      }

      // Remember what has been read, and how far, for "continue reading" and the read chips.
      var saveTimer;
      var save = function () {
        var r = body.getBoundingClientRect(), pct = Math.round(Math.min(100, Math.max(0, -r.top / Math.max(1, r.height - innerHeight) * 100)));
        var hist = store('bd-history') || [], old = hist.filter(function (h) { return h.u === here; })[0];
        pct = Math.max(pct, old ? old.p : 0);
        hist = [{ u: here, t: title2, p: pct, d: Date.now() }].concat(hist.filter(function (h) { return h.u !== here; })).slice(0, 200);
        store('bd-history', hist);
      };
      save();
      addEventListener('scroll', function () { clearTimeout(saveTimer); saveTimer = setTimeout(save, 600); }, { passive: true });

      // Faster pages: load images further down lazily.
      Array.prototype.slice.call(body.querySelectorAll('img'), 2).forEach(function (img) {
        if (!img.hasAttribute('loading')) img.setAttribute('loading', 'lazy');
        img.setAttribute('decoding', 'async');
      });
    }

    // External links open in a new tab, so readers keep their place on the blog.
    document.querySelectorAll('.post-body a[href^="http"]').forEach(function (a) {
      if (a.hostname && a.hostname !== location.hostname && !a.target) { a.target = '_blank'; a.rel = (a.rel ? a.rel + ' ' : '') + 'noopener'; }
    });

    // Read chips on post cards, and a "continue reading" link on the homepage.
    var seen = store('bd-history') || [], readUrls = {};
    seen.forEach(function (h) { if (h.p >= 60) readUrls[h.u] = 1; });
    document.querySelectorAll('.index-post').forEach(function (p) {
      var a = p.querySelector('.post-title a'), wrap = p.querySelector('.post-image-wrap');
      if (a && wrap && readUrls[cleanUrl(a.href)] && !wrap.querySelector('.bd-read-chip')) wrap.appendChild(el('span', 'bd-read-chip', '✓ Read'));
    });
    var hero2 = document.querySelector('.bd-front');
    if (isHome && hero2) {
      var last = seen.filter(function (h) { return h.p >= 5 && h.p < 85 && Date.now() - h.d < 30 * 864e5; })[0];
      if (last && !store('bd-resume-off-' + last.u)) {
        var res = el('section', 'bd-resume', '<a href="' + esc(last.u) + '"><i style="--p:' + last.p + '"></i><span><small>Continue reading</small><b>' + esc(last.t) + '</b></span><em>' + last.p + '% read</em><button type="button" aria-label="Dismiss">×</button></a>');
        res.querySelector('button').addEventListener('click', function (ev) { ev.preventDefault(); store('bd-resume-off-' + last.u, 1); res.remove(); });
        hero2.parentNode.insertBefore(res, hero2);
      }
    }

    // Topic pages: a magazine section page. Header with what the topic covers, its size and its guide, a lead story
    // with two more, then every article in the topic on one page with a filter box, then the other topics. Built from
    // the topic's summary feed; the theme's own list stays in place if the feed fails.
    var lm = path.match(/^\/search\/label\/([^/]+)$/);
    var cw = document.getElementById('content-wrapper');
    if (lm && cw && !document.querySelector('.bd-tp')) {
      var lab = decodeURIComponent(lm[1].replace(/\+/g, ' '));
      var info = TOPICS.filter(function (t) { return t[0] === lab; })[0];
      var about = (info && info[1]) || TOPIC_ABOUT[lab] || 'Practical, in-depth guides';
      var hubKey = HUB_LABELS[lab], hub = GUIDES.filter(function (g) { return g[0] === hubKey; })[0];
      var col = (COLORS[lab.toLowerCase()] || ['#e2462c'])[0];
      var others = TOPICS.map(function (t) { return t[0]; }).concat(Object.keys(TOPIC_ABOUT)).filter(function (t, i, a) { return t !== lab && a.indexOf(t) === i; });
      var tp = el('section', 'bd-tp', '<header class="bd-tp-head"><small>Section</small><h1>' + esc(lab) + '</h1><p>' + esc(about) + '.</p>' +
        '<div class="bd-tp-meta"><span class="bd-tp-count">Loading articles…</span>' + (hub ? '<a class="bd-tp-hub" href="' + hub[4] + '">New here? Start with the ' + hub[0].toLowerCase() + ' guide →</a>' : '') + '</div></header>' +
        '<div class="bd-tp-lead"><div class="bd-lead bd-skel"></div><div class="bd-second"><div class="bd-skel"></div><div class="bd-skel"></div></div></div>' +
        '<div class="bd-tp-bar"><h2>All ' + esc(lab.toLowerCase()) + ' articles</h2><input class="bd-tp-filter" type="search" placeholder="Filter these articles" aria-label="Filter articles in ' + esc(lab) + '"/></div>' +
        '<div class="bd-tp-grid"></div><p class="bd-tp-none" hidden>No articles in ' + esc(lab) + ' match that.</p>' +
        '<nav class="bd-tp-more" aria-label="Other sections"><h2>Other sections</h2><div>' + others.map(function (t) { return '<a href="' + labelUrl(t) + '">' + esc(t) + '</a>'; }).join('') + '</div></nav>');
      tp.style.setProperty('--c', col);
      cw.parentNode.insertBefore(tp, cw);
      root.classList.add('bd-topicpage');
      var tpPic = function (p, w) { return p.thumb ? sized(p.thumb, w) : cover(p.title, lab, true); };
      feed('', 60, function (posts) {
        if (!posts.length) { tp.remove(); root.classList.remove('bd-topicpage'); return; }
        root.classList.add('bd-topicpage-ready');
        tp.querySelector('.bd-tp-count').textContent = posts.length + ' article' + (posts.length === 1 ? '' : 's') + ' · latest ' + longDate(posts[0].date);
        var withPic = posts.filter(function (p) { return p.thumb; }), used = {};
        var lead = withPic[0] || posts[0]; used[lead.url] = 1;
        var second = withPic.filter(function (p) { return !used[p.url]; }).slice(0, 2); second.forEach(function (p) { used[p.url] = 1; });
        tp.querySelector('.bd-tp-lead').innerHTML = '<div class="bd-lead"><a href="' + esc(lead.url) + '"><span class="bd-lead-img"><img alt="" fetchpriority="high" decoding="async" src="' + esc(tpPic(lead, 960)) + '"/></span>' +
          '<small class="bd-kick">Featured</small><h1>' + esc(lead.title) + '</h1>' + (lead.sum ? '<p>' + esc(lead.sum) + '</p>' : '') + '<span class="bd-meta">' + esc(longDate(lead.date)) + '</span></a></div>' +
          '<div class="bd-second">' + second.map(function (p) {
            return '<a class="bd-story" href="' + esc(p.url) + '"><span class="bd-story-img"><img alt="" decoding="async" src="' + esc(tpPic(p, 640)) + '"/></span><b>' + esc(p.title) + '</b><span class="bd-meta">' + esc(longDate(p.date)) + '</span></a>';
          }).join('') + '</div>';
        var grid = tp.querySelector('.bd-tp-grid');
        grid.innerHTML = posts.map(function (p) {
          return '<a class="bd-tp-card' + (used[p.url] ? ' bd-tp-dup' : '') + '" href="' + esc(p.url) + '" data-t="' + esc(p.title.toLowerCase()) + '"><span class="bd-sec-img"><img alt="" loading="lazy" decoding="async" src="' + esc(tpPic(p, 640)) + '"/></span>' +
            '<b>' + esc(p.title) + '</b><small>' + esc(longDate(p.date)) + '</small></a>';
        }).join('');
        var none = tp.querySelector('.bd-tp-none');
        tp.querySelector('.bd-tp-filter').addEventListener('input', function () {
          var q = this.value.trim().toLowerCase(), shownN = 0;
          tp.querySelector('.bd-tp-lead').hidden = !!q;
          tp.classList.toggle('bd-tp-filtering', !!q);  // the lead stories' cards show in the grid only while filtering
          grid.querySelectorAll('.bd-tp-card').forEach(function (c) { var on = !q || c.getAttribute('data-t').indexOf(q) > -1; c.hidden = !on; if (on && (q || !c.classList.contains('bd-tp-dup'))) shownN++; });
          none.hidden = shownN > 0;
        });
      }, lab);
    }

    // Search results: a search page with the query in a large box, results ranked title matches first with the
    // words highlighted, topic filters with counts, 20 at a time, and a helpful page when nothing matches.
    // Built from the summary feed; the theme's list stays if the feed fails.
    var sq = location.search.match(/[?&]q=([^&]*)/);
    var isSearch = path === '/search' && !/[?&](updated-max|start|by-date)=/.test(location.search);
    if (isSearch && cw && !document.querySelector('.bd-sr')) {
      var query = sq ? decodeURIComponent(sq[1].replace(/\+/g, ' ')).trim() : '';
      var terms = query.toLowerCase().split(/\s+/).filter(function (t) { return t.length > 1; });
      var termRe = terms.length ? new RegExp('(' + terms.map(function (t) { return t.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); }).join('|') + ')', 'gi') : null;
      var hl = function (text) { var e = esc(text); return termRe ? e.replace(termRe, '<mark>$1</mark>') : e; };
      var sr = el('section', 'bd-sr', '<header class="bd-sr-head"><small>Search</small>' +
        '<form class="bd-sr-form" action="/search" method="get" role="search"><input name="q" type="search" value="' + esc(query) + '" placeholder="Search 160+ articles" aria-label="Search articles"/><button type="submit">Search</button></form>' +
        '<p class="bd-sr-count">' + (query ? 'Searching…' : 'Search every guide on Blog Digest, or browse a section.') + '</p><div class="bd-sr-chips"></div></header>' +
        '<div class="bd-sr-list"></div><button class="bd-sr-more" type="button" hidden>Show more results</button><div class="bd-sr-empty" hidden></div>');
      cw.parentNode.insertBefore(sr, cw);
      root.classList.add('bd-searchpage');
      var list = sr.querySelector('.bd-sr-list'), more = sr.querySelector('.bd-sr-more'), chips = sr.querySelector('.bd-sr-chips');
      var srPic = function (p) { return p.thumb ? sized(p.thumb, 400) : cover(p.title, p.label, false); };
      var row = function (p) {
        return '<a class="bd-sr-row" href="' + esc(p.url) + '"><span class="bd-sr-img"><img alt="" loading="lazy" decoding="async" src="' + esc(srPic(p)) + '"/></span>' +
          '<span class="bd-sr-text"><small>' + esc(p.label || 'Article') + ' · ' + esc(longDate(p.date)) + '</small><b>' + hl(p.title) + '</b>' + (p.sum ? '<span class="bd-sr-sum">' + hl(p.sum) + '</span>' : '') + '</span></a>';
      };
      var sectionChips = function () {
        return TOPICS.map(function (t) { return '<a class="bd-chip" href="' + labelUrl(t[0]) + '">' + esc(t[0]) + '</a>'; }).join('');
      };
      var showEmpty = function (latest) {
        var box = sr.querySelector('.bd-sr-empty'); box.hidden = false;
        box.innerHTML = '<h2>No articles match “' + esc(query) + '”</h2><ul><li>Check the spelling, or try a shorter or more general word.</li><li>Search for a topic, such as “loan”, “sleep” or “career”.</li></ul>' +
          '<h3>Browse a section</h3><div class="bd-sr-chips">' + sectionChips() + '</div><h3>Latest articles</h3><div class="bd-sr-list">' + latest.slice(0, 6).map(row).join('') + '</div>';
      };
      if (!query) {
        chips.innerHTML = sectionChips();
        feed('', 12, function (posts) { root.classList.add('bd-searchpage-ready'); list.innerHTML = '<h2 class="bd-sr-sub">Latest articles</h2>' + posts.map(row).join(''); });
      } else {
        feed(query, 100, function (posts, failed) {
          if (failed) { sr.remove(); root.classList.remove('bd-searchpage'); return; }
          root.classList.add('bd-searchpage-ready');
          var score = function (p) {  // phrase in the title, then words in the title, then words in the excerpt or topic
            var t = p.title.toLowerCase(), x = (p.sum + ' ' + p.label).toLowerCase();
            return (t.indexOf(query.toLowerCase()) > -1 ? 50 : 0) + terms.filter(function (w) { return t.indexOf(w) > -1; }).length * 10 + terms.filter(function (w) { return x.indexOf(w) > -1; }).length * 3;
          };
          posts = posts.map(function (p, i) { return { p: p, s: score(p), i: i }; }).sort(function (a, b) { return b.s - a.s || a.i - b.i; }).map(function (x) { x.p.strong = x.s >= 10; return x.p; });
          sr.querySelector('.bd-sr-count').innerHTML = posts.length ? '<b>' + posts.length + '</b> result' + (posts.length === 1 ? '' : 's') + ' for “' + esc(query) + '”' : '';
          if (!posts.length) { feed('', 6, showEmpty); return; }
          var counts = {}; posts.forEach(function (p) { if (p.label) counts[p.label] = (counts[p.label] || 0) + 1; });
          var labels = Object.keys(counts).sort(function (a, b) { return counts[b] - counts[a]; });
          chips.innerHTML = labels.length > 1 ? '<button type="button" class="bd-chip bd-on" data-l="">All <i>' + posts.length + '</i></button>' +
            labels.map(function (l) { return '<button type="button" class="bd-chip" data-l="' + esc(l) + '">' + esc(l) + ' <i>' + counts[l] + '</i></button>'; }).join('') : '';
          var current = posts, shownN = 0;
          var render = function (reset) {
            if (reset) { list.innerHTML = ''; shownN = 0; }
            var hasStrong = current.some(function (p) { return p.strong; });
            list.insertAdjacentHTML('beforeend', current.slice(shownN, shownN + 20).map(function (p, k) {
              var idx = shownN + k, prev = current[idx - 1];
              var sub = hasStrong && !p.strong && (idx === 0 || (prev && prev.strong)) ? '<h2 class="bd-sr-sub bd-sr-also">Also mentions \u201c' + esc(query) + '\u201d</h2>' : '';
              return sub + row(p);
            }).join(''));
            shownN = Math.min(current.length, shownN + 20);
            more.hidden = shownN >= current.length;
            more.textContent = 'Show more results (' + (current.length - shownN) + ' more)';
          };
          render(true);
          more.addEventListener('click', function () { render(false); });
          chips.addEventListener('click', function (ev) {
            var b = ev.target.closest('button.bd-chip'); if (!b) return;
            chips.querySelectorAll('.bd-chip').forEach(function (c) { c.classList.toggle('bd-on', c === b); });
            var l = b.getAttribute('data-l');
            current = l ? posts.filter(function (p) { return p.label === l; }) : posts;
            render(true);
          });
        });
      }
    }

    // 404 page: search, the topic guides and the latest articles instead of a dead end.
    var err = document.querySelector('.errorWrap');
    if (err && !document.querySelector('.bd-404')) {
      var box404 = el('div', 'bd-404',
        '<form class="bd-search" action="/search" method="get" role="search"><input name="q" type="search" placeholder="Search 160+ articles…" aria-label="Search articles"/><button type="submit">Search</button></form>' +
        '<div class="bd-search-results"></div>' +
        '<div class="bd-404-grid">' + GUIDES.map(function (g) { return '<a href="' + g[4] + '">' + g[0] + ' guide →</a>'; }).join('') + '</div>' +
        '<h3 class="bd-404-sub">Latest articles</h3><div class="bd-404-grid bd-404-latest"></div>');
      err.appendChild(box404);
      instantSearch(box404.querySelector('input'), box404.querySelector('.bd-search-results'));
      feed('', 6, function (posts) {
        box404.querySelector('.bd-404-latest').innerHTML = posts.map(function (p) { return '<a href="' + esc(p.url) + '">' + esc(p.title) + '</a>'; }).join('');
      });
    }

    // Back to top + sticky header shadow.
    var top = el('button', '', ICONS.up); top.id = 'bd-top'; top.type = 'button'; top.setAttribute('aria-label', 'Back to top');
    top.addEventListener('click', function () { scrollTo({ top: 0, behavior: 'smooth' }); });
    document.body.appendChild(top);
    var onScroll = function () {
      top.classList.toggle('bd-show', scrollY > 700);
      if (header) header.classList.toggle('bd-scrolled', scrollY > 10);
    };
    addEventListener('scroll', onScroll, { passive: true }); onScroll();

    // Reveal on scroll, for elements below the fold only; everything shows after 3 seconds regardless.
    var targets = document.querySelectorAll('.index-post, #sidebar-wrapper .widget, .bd-topic, .seo-related, .blog-post-comments, .bd-closer');
    if ('IntersectionObserver' in window) {
      var io = new IntersectionObserver(function (es) {
        es.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('bd-in'); io.unobserve(e.target); } });
      }, { rootMargin: '0px 0px -40px 0px' });
      targets.forEach(function (t, i) {
        if (t.getBoundingClientRect().top < innerHeight) return;
        t.classList.add('bd-reveal'); t.style.transitionDelay = (i % 4) * 70 + 'ms'; io.observe(t);
      });
      setTimeout(function () { targets.forEach(function (t) { t.classList.add('bd-in'); }); }, 3000);
    }
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();
})();