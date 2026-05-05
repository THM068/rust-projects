use crate::state::SharedAppState;
use axum::handler::Handler;
use axum::http::StatusCode;
use axum::response::IntoResponse;
use axum::routing::get;
use axum::{Json, Router};
use serde::{Deserialize, Serialize};
#[derive(Debug, Serialize, Deserialize)]
pub struct Health {
    pub status: String,
}
pub fn health_routes(shared_state: SharedAppState) -> Router{
    
    pub async fn health() -> impl IntoResponse {
        (StatusCode::OK, Json(Health{status: "OK".to_string()}))

    }
    Router::new()
        .route("/health", get(health))
        .with_state(shared_state)
}