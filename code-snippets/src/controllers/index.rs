use askama::Template;
use spring_web::axum::response::IntoResponse;
use spring_web::get;
use crate::controllers::view::render;

#[get("/")]
pub async fn chat_page() -> impl IntoResponse {
    let template = HomePageTemplate {};
    render(template)
}

#[derive(Template)]
#[template(path = "home.html")]
struct HomePageTemplate {
}