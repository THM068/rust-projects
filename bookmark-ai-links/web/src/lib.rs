use spin_sdk::http::{IntoResponse, Request, Router};
use spin_sdk::http_component;
use askama::Template;
use controllers::user;

mod controllers;
mod models;

/// A simple Spin HTTP component.
#[http_component]
fn handle_ai_bookmark_app(req: Request) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    let mut router = Router::default();

    router.get("/", controllers::home::handle_home);
    router.get("/about", controllers::home::handle_about);
    router.post("/add-name", controllers::home::handle_add_name);
    router.post("/bookmark", controllers::bookmarks::handle_add_bookmark);
    router.get("/bookmark", controllers::bookmarks::handle_get_all_bookmarks);
    
    //user routes
    router.post_async("/user", user::handle_add_user);
    router.get_async("/user/:id", user::handle_get_user_by_id);

    //authentication routes
    router.post_async("/login", controllers::auth::handle_login);
    
    //catch-all for 404 Not Found
    router.any("/*", controllers::home::handle_not_found);

    Ok(router.handle(req))
}
