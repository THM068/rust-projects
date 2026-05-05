use axum::extract::State;
use axum::http::StatusCode;
use axum::Json;
use axum::response::IntoResponse;
use crate::state::SharedAppState;
use axum::routing::post;
use tracing::info;
use spring_rs_gerust_db::entities;
use spring_rs_gerust_db::entities::users::{UserChangeset};
use spring_rs_gerust_db::Error::RecordAlreadyExists;
use crate::error::Error;

pub fn registration_routes(app_state: SharedAppState) -> axum::Router {
    #[axum::debug_handler]
    pub async fn register_user(State(app_state): State<SharedAppState>,Json(user): Json<UserChangeset>) -> Result<(StatusCode, Json<entities::users::User>), Error> {
        println!("Registering user: {:?}", user);
        match entities::users::find_by_username(user.username.clone(), &app_state.db_pool).await {
            Ok(_) => return Err(Error::Database(RecordAlreadyExists)),
            Err(_) => (),
        };
        let user_result = entities::users::create(user, &app_state.db_pool).await;

        match user_result {
            Ok(user) => {
                info!("responding with {:?}", user);
                Ok((StatusCode::CREATED, Json(user)))
            }
            Err(e) => Err(Error::Database(e)),
        }
    }
    
    axum::Router::new()
        .route("/register", post(register_user))
        .with_state(app_state)
}