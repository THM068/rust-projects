pub mod home;
pub mod bookmarks;
#[macro_use]
pub mod user;
#[macro_use]
pub mod auth;

pub fn redirect_to(url: &str) -> impl spin_sdk::http::IntoResponse {
    spin_sdk::http::Response::builder()
        .status(302)
        .header("Location", url)
        .build()
}

pub fn render_as_html(html: &str) -> impl spin_sdk::http::IntoResponse {
    spin_sdk::http::Response::builder()
        .status(OK_STATUS)
        .header("content-type", "text/html")
        .body(html.to_string())
        .build()
}

pub fn 
render_as_json(content: &str, status: u16) -> impl spin_sdk::http::IntoResponse {
    spin_sdk::http::Response::builder()
        .status(status)
        .header("content-type", "application/json")
        .body(content.to_string())
        .build()
}

pub fn render_as_json_error(content: &str, status: u16) -> impl spin_sdk::http::IntoResponse {
    spin_sdk::http::Response::builder()
        .status(status)
        .header("content-type", "application/json")
        .body(content.to_string())
        .build()
}




pub const OK_STATUS: u16 = 200;
//create all other status codes as constants
pub const CREATED_STATUS: u16 = 201;
pub const ACCEPTED_STATUS: u16 = 202;
pub const NO_CONTENT_STATUS: u16 = 204;
pub const BAD_REQUEST_STATUS: u16 = 400;
pub const UNAUTHORIZED_STATUS: u16 = 401;
pub const FORBIDDEN_STATUS: u16 = 403;
pub const NOT_FOUND_STATUS: u16 = 404;
pub const INTERNAL_SERVER_ERROR_STATUS: u16 = 500;
pub const NOT_IMPLEMENTED_STATUS: u16 = 501;
pub const BAD_GATEWAY_STATUS: u16 = 502;
pub const SERVICE_UNAVAILABLE_STATUS: u16 = 503;
pub const GATEWAY_TIMEOUT_STATUS: u16 = 504;


