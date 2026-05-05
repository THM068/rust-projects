mod model;

use spin_sdk::http::{IntoResponse, Request, Response};
use spin_sdk::http_component;
use std::collections::HashMap;
use crate::model::get_method;

#[derive(serde::Deserialize, Debug)]
struct StubConfig {
    stubs: Vec<Stub>,
}

#[derive(serde::Deserialize, Debug)]
struct Stub {
    name: String,
    request: RequestMatcher,
    response: ResponseTemplate,
}

#[derive(serde::Deserialize, Debug)]
struct RequestMatcher {
    method: String,
    path: String,
    #[serde(default)]
    body_contains: Option<String>,
}

#[derive(serde::Deserialize, Debug)]
struct ResponseTemplate {
    status: u16,
    #[serde(default)]
    headers: HashMap<String, String>,
    #[serde(default)]
    #[serde(default)]
    delay_ms: Option<u64>,
}

static STUBS: std::sync::OnceLock<Vec<Stub>> = std::sync::OnceLock::new();


/// A simple Spin HTTP component.
#[http_component]
fn handle_mocker_app(req: Request) -> anyhow::Result<impl IntoResponse> {
    println!("Handling request to {:?}", req.header("spin-full-url"));
    // Load stubs once (bundled in WASM)
    let stubs = STUBS.get_or_init(|| {
        let yaml = include_str!("../stubs.yaml");
        let config: StubConfig = serde_yaml::from_str(yaml).unwrap();
        config.stubs
    });

    let method = req.method();
    let uri_path = req.uri();

    
    for stub in stubs {
        if get_method(stub.request.method.as_str()) == *method && stub.request.path == uri_path {
            // Optional: check body_contains
            if let Some(ref needle) = stub.request.body_contains {
                let body = String::from_utf8_lossy(req.body());
                if !body.to_string().contains(needle) {
                    continue;
                }
            }

            // Simulate delay (if any)
            if let Some(delay) = stub.response.delay_ms {
                std::thread::sleep(std::time::Duration::from_millis(delay));
            }

                    http::header::HeaderValue::from_str(v).unwrap(),
                );
            }

            return Ok(response);
        }
    }
    // 
    // // No match → 404
    // Ok(Response::builder().status(404).body("No stub found")?)

    Ok(Response::builder()
        .status(200)
        .header("content-type", "text/html")
        .body("body")
        .build())
}
