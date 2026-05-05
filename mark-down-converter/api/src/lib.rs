mod api;
mod handlers;

use spin_sdk::http::{IntoResponse, Request, Response};
use spin_sdk::http_component;
/// A simple Spin HTTP component.
#[http_component]
fn handle_api(req: Request) -> anyhow::Result<impl IntoResponse> {
    println!("Hello ..... from API component!");
    let api = api::Api::default();
    api.handle(req)
}
