cc.game.onStart = function(){
    cc.view.setDesignResolutionSize(800, 450, cc.ResolutionPolicy.SHOW_ALL);
	cc.view.resizeWithBrowserSize(true);
    //load resources
    cc.LoaderScene.preload(g_resources, function () {
        //cc.director.runScene(new GameScene());
        cc.director.runScene(new LoginScene());
        //cc.director.runScene(new SandboxScene());
    }, this);
};
cc.game.run();