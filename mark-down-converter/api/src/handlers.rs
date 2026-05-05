use pulldown_cmark::{html, Options, Parser};
use spin_sdk::http;
use spin_sdk::http::{Request, Response};

pub fn convertMarkdownHandler(request: Request, _params: spin_sdk::http::Params) -> anyhow::Result<impl spin_sdk::http::IntoResponse, anyhow::Error> {
    println!("Handling request to convert markdown {:?}", request.header("spin-full-url"));

    let Ok(markdown_text) = std::str::from_utf8(request.body().as_ref()) else {
        return Ok(http::Response::builder()
            .status(400)
            .body("No markdown content provided")
                      .build())
    };
    
    if markdown_text.is_empty() {
        return Ok(http::Response::builder()
            .status(400)
            .body("No markdown content provided")
                      .build())
    }

    let parser = Parser::new_ext(markdown_text, Options::all());
    let mut html_output = String::new();
    html::push_html(&mut html_output, parser);
    
    Ok(Response::builder()
        .status(200)
        .header("content-type", "text/plain")
        .body(html_output)
        .build())
}