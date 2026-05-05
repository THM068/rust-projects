use crate::controllers::{redirect_to, render_as_html};
use askama::Template;
use serde::{Deserialize, Serialize};
use spin_sdk::http::StatusCode;
use spin_sdk::http::{IntoResponse, Params, Request, Response};
use std::io::Bytes;
use validator::{Validate, ValidationError};

pub fn handle_add_name(
    req: Request,
    params: Params,
) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    println!(
        "Handling request to add name {:?}",
        req.header("spin-full-url")
    );
    println!("Params: {:?}", params);

    let person: Person = serde_urlencoded::from_bytes(req.body().as_ref())?;

    println!("Person: {:?}", person);

    Ok(redirect_to("/"))
}
pub fn handle_home(
    req: Request,
    _params: Params,
) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    println!("Handling request to home {:?}", req.header("spin-full-url"));

    let template = HelloTemplate {
        name: "World".to_string(),
    };

    Ok(render_as_html(&template.render()?))
}

pub fn handle_about(
    req: Request,
    _params: Params,
) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    println!("Handling request to home {:?}", req.header("spin-full-url"));
    let template = AboutTemplate {};

    Ok(render_as_html(&template.render()?))
}

pub fn handle_not_found(
    req: Request,
    _params: Params,
) -> anyhow::Result<impl IntoResponse, anyhow::Error> {

    let template = NotFoundTemplate {};

    Ok(render_as_html(&template.render()?))
}

#[derive(Template)]
#[template(path = "index.html")]
struct HelloTemplate {
    name: String,
}

#[derive(Template)]
#[template(path = "about.html")]
struct AboutTemplate {}

#[derive(Template)]
#[template(path = "notfound.html")]
struct NotFoundTemplate {}

#[derive(Debug, Serialize, Deserialize, Validate)]
struct Person {
    #[validate(length(min = 1, message = "Name cannot be empty"))]
    name: String,
}
