use crate::controllers::greeting;
use crate::controllers::greeting::greeting_routes;
use crate::controllers::home::home_routes;
use crate::controllers::ledger::ledger_routes;
use crate::state::AppState;
use axum::{routing::get, Router};
use std::sync::Arc;
use socketioxide::extract::{SocketRef, Data, AckSender};
use socketioxide::handler::Value;
use socketioxide::SocketIo;
use tower_http::cors::{Any, CorsLayer};
use tower_http::services::ServeDir;
use tracing::info;
use crate::controllers::balances::balances_routes;
use crate::controllers::health::health_routes;

/// Initializes the application's routes.
///
/// This function maps paths (e.g. "/greet") and HTTP methods (e.g. "GET") to functions in [`crate::controllers`] as well as includes middlewares defined in [`crate::middlewares`] into the routing layer (see [`axum::Router`]).
pub fn init_routes(app_state: AppState) -> Router {
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let (socket_io_layer, io) = SocketIo::new_layer();
    io.ns("/chat", on_connect);
    let shared_app_state = Arc::new(app_state);

    Router::new()
        .merge(home_routes(Arc::clone(&shared_app_state)))
        .merge(health_routes(Arc::clone(&shared_app_state)))
        .merge(ledger_routes(Arc::clone(&shared_app_state)))
        .merge(balances_routes(Arc::clone(&shared_app_state)))
        .nest("/api", greeting_routes(Arc::clone(&shared_app_state)))
        .nest_service("/assets", ServeDir::new("static"))
        .layer(cors)
        .layer(socket_io_layer)
}

pub fn on_connect(socket: SocketRef) {
    info!("Socket connected: {:?}", socket.id);
    socket.emit("auth", "hello world").ok();
    socket.on("msg",  async move |socket: SocketRef, Data(data): Data<Value>| {
        info!("Message received: {:?}", data);
    });

    socket.on(
        "auth",
        async move |Data::<Value>(data), ack: AckSender| {
            info!(?data, "Received event");
            ack.send(&data).ok();
        },
    );
}

