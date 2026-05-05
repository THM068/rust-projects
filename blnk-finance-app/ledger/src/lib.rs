
use spin_sdk::http::{IntoResponse, Request, Router};
use spin_sdk::http_component;
mod controllers;

/// A simple Spin HTTP component.
#[http_component]
fn handle_ledger(req: Request) -> anyhow::Result<impl IntoResponse> {
    let mut router = Router::new();

    router.get_async("/api/ledger/:ledger-id", controllers::handlers::get_ledger);
    router.post_async("/api/ledger", controllers::handlers::create_ledger);
    //router.put_async("/api/ledger", controllers::handlers::update_ledger);

    Ok(router.handle(req))

}
