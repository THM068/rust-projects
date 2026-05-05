use crate::controllers::view::HtmlTemplate;
use crate::state::SharedAppState;
use askama::Template;
use axum::extract::{Form, Path, State};
use axum::response::{IntoResponse, Redirect};
use axum::routing::{get, post};
use axum::{Json, Router};
use axum::extract::rejection::JsonRejection::JsonDataError;
use serde::Deserialize;
use tracing::info;
use tracing::log::error;
use validator::Validate;
use newsletter_app_blnk_client::{ApiError, ApiResponse, CreateLedgerRequest, Ledger};
use crate::error::Error;

#[derive(Template)]
#[template(path = "ledger/create_ledger.html")]
struct CreateLedgerTemplate {}

impl CreateLedgerTemplate {
    fn new() -> Self {
        CreateLedgerTemplate {}
    }
    
    fn render() -> HtmlTemplate<CreateLedgerTemplate> {
        let template = CreateLedgerTemplate::new();
        HtmlTemplate(template)
    }
}

#[derive(Deserialize, Validate, Debug)]
pub struct CreateLedgerForm {
    #[validate(length(min = 1, message = "Ledger name must not be empty"))]
    name: String,
}

pub fn ledger_routes(shared_state: SharedAppState) -> Router {
    
    pub async fn ledgers(State(app_state): State<SharedAppState>) -> Result<Json<Vec<Ledger>>, Error> {
        let Ok(ledgers) = app_state.blnk_client.list_ledgers().await else {
            return Err(anyhow::anyhow!("Error listing ledgers").into());
        };
        
        Ok(Json(ledgers))
    } 
    async fn create_ledger_page() -> impl IntoResponse {
        info!("Rendering create ledger page");
        CreateLedgerTemplate::render()
    }

    async fn handle_create_ledger(State(app_state): State<SharedAppState>, Form(form): Form<CreateLedgerForm>) -> impl IntoResponse {
        info!("Creating ledger with name: {}", form.name);
        // Here you would typically save the ledger to a database
        // For now, we'll just redirect back to the home page
        let create_ledger_request = CreateLedgerRequest{
            name: form.name,
            metadata: None,
        };
        let ledger_create_response = app_state.blnk_client.create_ledger(create_ledger_request).await;
        match  ledger_create_response {  
           Ok(ledger_create) => {
                info!("Ledger created: {:?}", ledger_create);
                Redirect::to("/")   
            },
            Err(e) => {
                error!("Error creating ledger: {}", e);
                Redirect::to("/create-ledger")
            }
        }
    }
    
    pub async fn get_ledger(Path(ledger_id): Path<String>, State(app_state): State<SharedAppState>) -> Result<Json<Ledger>, Error> {
        let Ok(ledger) = app_state.blnk_client.get_ledger(ledger_id.as_str()).await else {
            error!("Error getting ledger");
            return Err(anyhow::anyhow!("Error getting ledger").into());
        };
        info!("Ledger found: {:?}", ledger);
        Ok(Json(ledger))
    }

    Router::new()
        .route("/ledger", get(ledgers).post(handle_create_ledger))
        .route("/ledger/{ledger_id}", get(get_ledger))
        .with_state(shared_state)
}