use crate::state::SharedAppState;
use axum::handler::Handler;
use axum::response::Json;
use axum::routing::get;
use axum::Router;
use serde::{Deserialize, Serialize};

/// A greeting to respond with to the requesting client
#[derive(Deserialize, Serialize)]
pub struct Greeting {
    /// Who do we say hello to?
    pub hello: String,
}

pub fn greeting_routes(shared_state: SharedAppState) -> Router {
    #[axum::debug_handler]
    pub async fn hello() -> Json<Greeting> {
        Json(Greeting {
            hello: String::from("world"),
        })
    }
    Router::new()
        .route("/", get(hello))
        .with_state(shared_state)
}
