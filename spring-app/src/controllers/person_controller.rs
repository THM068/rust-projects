use askama::Template;
use rig::client::completion::CompletionClientDyn;
use rig::completion::Prompt;
use rig::providers;
use serde_json::json;
use spring_web::axum::http::status::StatusCode;
use spring_web::axum::response::IntoResponse;
use spring_web::axum::Json;
use spring_web::axum::Form;
use spring_web::extractor::Path;
use spring_web::{get, post, route};
use serde::{Deserialize, Serialize};
use crate::ai_client;
use crate::controllers::view::{render};

#[get("/")]
pub async fn hello_world() -> impl IntoResponse {
    let deepseek_client = ai_client::deep_seek_client::DeepSeekClient::new();
    let agent = deepseek_client.client.agent(providers::deepseek::DEEPSEEK_CHAT)
        .preamble("You are a calculator here to help the user perform arithmetic operations.")
        .max_tokens(1024)
        .build();

    let result = agent.prompt("What is 2 + 2?").await;

    match result {
        Ok(response) => {
            format!("answer is {response}")
        },
        Err(err) => {
            format!("error happened: {err}")
        }
    }
   }
#[route("/hello/{name}", method = "GET", method = "POST")]
async fn hello(Path(name): Path<String>) -> impl IntoResponse {
    format!("hello {name}")
}

#[get("/hello")]
async fn index() -> impl IntoResponse {
    (
        StatusCode::OK,
        Json(json!({
            "message": "Hello, World!",
            "status": "success"
        }))
    )
}

#[get("/chat")]
pub async fn chat_page() -> impl IntoResponse {
    let template = HomePageTemplate{
        show_messages: false,
        user_message: String::new(),
        ai_response: String::new(),
    };
    render(template)
}
#[get("/health")]
async fn health_check() -> impl IntoResponse {
    (StatusCode::OK, "Service is healthy")
}

#[post("/chat")]
pub async fn chat(Form(form): Form<ChatForm>) -> impl IntoResponse {
    let user_prompt = form.prompt;

    // Create DeepSeek client
    let deepseek_client = ai_client::deep_seek_client::DeepSeekClient::new();

    // Create an agent with appropriate settings
    let agent = deepseek_client.client.agent(providers::deepseek::DEEPSEEK_CHAT)
        .preamble("You are an epidemiologist and vaccine expert. You give concise, accurate answers to questions about vaccines and epidemiology.")
        .max_tokens(1024)
        .build();

    // Send the prompt to the LLM
    let result = agent.prompt(&user_prompt).await;

    // Create template with user message and AI response
    let template = match result {
        Ok(response) => {
            HomePageTemplate {
                show_messages: true,
                user_message: user_prompt,
                ai_response: response,
            }
        },
        Err(err) => {
            HomePageTemplate {
                show_messages: true,
                user_message: user_prompt,
                ai_response: format!("Error: {}", err),
            }
        }
    };

    render(template)
}

#[post("/chat/htmx")]
pub async fn chat_htmx(Form(form): Form<ChatForm>) -> impl IntoResponse {
    let user_prompt = form.prompt;

    // Create DeepSeek client
    let deepseek_client = ai_client::deep_seek_client::DeepSeekClient::new();

    // Create an agent with appropriate settings
    let agent = deepseek_client.client.agent(providers::deepseek::DEEPSEEK_CHAT)
        .preamble("You are an epidemiologist and vaccine expert. You give concise, accurate answers to questions about vaccines and epidemiology")
        .max_tokens(1024)
        .build();

    // Send the prompt to the LLM
    let result = agent.prompt(&user_prompt).await;

    // Create template with user message and AI response
    let template = match result {
        Ok(response) => {
            println!("response: {}", response);
            ChatMessagesTemplate {
                user_message: user_prompt,
                ai_response: response,
            }
        },
        Err(err) => {
            ChatMessagesTemplate {
                user_message: user_prompt,
                ai_response: format!("Error: {}", err),
            }
        }
    };

    render(template)
}

#[derive(Debug, Serialize, Deserialize)]
struct Person {
    name: String,
    age: u8,
}

#[derive(Debug, Deserialize)]
struct ChatForm {
    prompt: String,
}

#[derive(Template)]
#[template(path = "home.html")]
struct HomePageTemplate{
    show_messages: bool,
    user_message: String,
    ai_response: String,
}

#[derive(Template)]
#[template(path = "chat_messages.html")]
struct ChatMessagesTemplate{
    user_message: String,
    ai_response: String,
}
