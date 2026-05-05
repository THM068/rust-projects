use crate::controllers::greeting::Greeting;
use crate::controllers::view::HtmlTemplate;
use crate::state::SharedAppState;
use askama::Template;
use axum::http::StatusCode;
use axum::response::IntoResponse;
use axum::routing::get;
use axum::{Json, Router};
use tracing::info;

#[derive(Template)]
#[template(path = "home.html")]
struct HomePageTemplate {
    names: Vec<String>,
}

impl HomePageTemplate {
    fn new(names: Vec<String>) -> Self {
        HomePageTemplate { names }
    }
    
    fn render(list: Vec<String>) -> HtmlTemplate<HomePageTemplate> {
        let template = HomePageTemplate::new(list);
        HtmlTemplate(template)
    }
}

pub fn home_routes(shared_state: SharedAppState) -> Router {
    /// Responds with a [`Greeting`], encoded as JSON.
    #[axum::debug_handler]
    pub async fn index() -> impl IntoResponse {
        info!("responding with {:?}", StatusCode::OK);
        HomePageTemplate::render(vec!["Uptime API".to_string(), "Home".to_string()])
    }

    Router::new()
        .route("/", get(index))
        .with_state(shared_state)
}
