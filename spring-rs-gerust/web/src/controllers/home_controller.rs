use askama::Template;
use axum::response::IntoResponse;
use axum::Router;
use crate::controllers::view::render;
use crate::state::{SharedAppState};

pub fn home_routes(state: SharedAppState) -> axum::Router {
    #[axum::debug_handler]
    pub async fn index() -> impl IntoResponse {
        render(HomePageTemplate{})
    }
    
    Router::new()
        .route("/", axum::routing::get(index))
        .with_state(state)
}

#[derive(Template)]
#[template(path = "home.html")]
struct HomePageTemplate {
}