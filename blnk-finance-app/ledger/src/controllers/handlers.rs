use crate::controllers::models::{CreateLedger, ErrorResponse, Ledger};
use crate::controllers::render_as_json;
use crate::controllers::request_payloads::get_legder_request_payload;
use spin_sdk::http::{IntoResponse, Method, Params, Request, Response};
use spin_sdk::variables;

pub async fn get_ledger(request: Request, params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    let Some(ledger_id) = params.get("ledger-id") else {
        let error_response: ErrorResponse = ErrorResponse::new("Bad Request");
        return Ok(render_as_json(&serde_json::to_string(&error_response)?, 400));
    };

    let response: Response = spin_sdk::http::send(get_legder_request_payload(ledger_id)).await?;
    match response.status() {
        200 => {
            let response_body:Ledger = serde_json::from_slice(response.body())?;
            Ok(render_as_json(&serde_json::to_string(&response_body)?, 200))
            
        },
        400 => {
            let error_response: ErrorResponse = ErrorResponse::new("Bad Request");
            Ok(render_as_json(&serde_json::to_string(&error_response)?, 400))
        },
        403 => {
            let error_response: ErrorResponse = ErrorResponse::new("Forbidden");
            Ok(render_as_json(&serde_json::to_string(&error_response)?, 403))
        },
        404 => {
            let error_response: ErrorResponse = ErrorResponse::new("Not Found");
            Ok(render_as_json(&serde_json::to_string(&error_response)?, 404))
        },
        _ => Ok(Response::builder()
            .status(500)
            .header("content-type", "text/plain")
            .build())
    }
}

// pub async fn create_ledger(request: Request, _params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
//     todo!()
// }    
pub async fn create_ledger(request: Request, _params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    let blnk_base_url = variables::get("blnk_base_uri").unwrap_or_else(|_| "unknown".to_string());
    let Ok(model) = CreateLedger::try_from(request.body()) else {
        return Ok(Response::new(400, "Bad Request"));
    };
    let json_payload = serde_json::to_string(&model)?;
    let pay_load_request = Request::builder()
        .method(Method::Post)
        .uri(format!("{}/ledgers", blnk_base_url))
        .header("content-type", "application/json")
        .body(json_payload).build();

    let response: Response = spin_sdk::http::send(pay_load_request).await?;
    match response.status() {
        201 => {
            let response_body:Ledger = serde_json::from_slice(response.body())?;
            Ok(render_as_json(&serde_json::to_string(&response_body)?, 201))
            
        },
        400 => {
            let error_response: ErrorResponse = ErrorResponse::new("Bad Request");
            Ok(render_as_json(&serde_json::to_string(&error_response)?, 400))
        },
        403 => {
            let error_response: ErrorResponse = ErrorResponse::new("Forbidden");
            Ok(render_as_json(&serde_json::to_string(&error_response)?, 403))
        },
        404 => {
            let error_response: ErrorResponse = ErrorResponse::new("Not Found");
            Ok(render_as_json(&serde_json::to_string(&error_response)?, 404))
        },
        _ => Ok(Response::builder()
            .status(500)
            .header("content-type", "text/plain")
            .build())
    }
    
}