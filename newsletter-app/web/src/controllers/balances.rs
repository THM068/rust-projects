use crate::error::Error;
use crate::state::SharedAppState;
use axum::extract::{Path, State};
use axum::routing::{get, post};
use axum::{Json, Router};
use newsletter_app_blnk_client::{Balance, CreateBalanceRequest};

pub fn balances_routes(shared_state: SharedAppState) -> Router {
    async fn create_balance(
        State(app_state): State<SharedAppState>,
        Json(create_balance_request): Json<CreateBalanceRequest>,
    ) -> Result<Json<Balance>, Error> {
        let balance_report_result = app_state
            .blnk_client
            .create_balance(create_balance_request)
            .await;

        match balance_report_result {
            Ok(response) => Ok(Json(response)),
            Err(err) => Err(anyhow::anyhow!("Error creating balance {}", err).into()),
        }
    }

    async fn get_balance(
        State(app_state): State<SharedAppState>,
        Path(balance_id): Path<String>,
    ) -> Result<Json<Balance>, Error> {
        let balance_result = app_state.blnk_client.get_balance(balance_id.as_str()).await;

        match balance_result {
            Ok(balance) => Ok(Json(balance)),
            Err(err) =>  Err(anyhow::anyhow!("Error getting balance {}", err).into()),
        }
    }

    Router::new()
        .route("/balances", post(create_balance))
        .route("/balances/{balance_id}", get(get_balance))
        .with_state(shared_state)
}
