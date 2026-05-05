use jwt_simple::claims::JWTClaims;
use serde::{Deserialize, Serialize};
use spin_sdk::http::{IntoResponse, Json, Params, Request, Response};
use validator::Validate;
use crate::controllers::{render_as_json, BAD_REQUEST_STATUS, render_as_json_error, OK_STATUS, UNAUTHORIZED_STATUS, CREATED_STATUS};
use crate::controllers::auth::{Auth, UserClaims};

use crate::models::users::{get_user_by_username, insert_user};
use crate::protected_route;

#[derive(Debug, Serialize, Deserialize, Validate)]
pub struct UserChangeset {
    #[validate(length(min = 1, message = "Username cannot be empty"))]
    pub username: String,
    #[validate(length(min = 1, message = "password cannot be empty"))]
    pub password: String,
}
pub async fn handle_get_user_by_id(req: Request, _params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    let claims = match Auth::verify(&req) {
        Ok(claims) => claims,
        Err(e) => return Ok(render_as_json("Unauthorized", UNAUTHORIZED_STATUS)),
    };
            
    Ok(render_as_json("Not implemented", OK_STATUS))
    
}

pub async fn handle_add_user(req: Request, _params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    // Placeholder implementation

    let changeset: UserChangeset = match serde_json::from_slice(req.body()) {
        Ok(data) => data,
        Err(e) => {
            return Ok(render_as_json(&format!(r#"{{"error": "Invalid JSON: {}"}}"#, e), BAD_REQUEST_STATUS));
        }
    };

    println!("User Changeset: {:?}", changeset);
    match changeset.validate() {
        Ok(_) => (),
        Err(e) => {
            let error_message = e.field_errors().iter().map(|(field, errors)| {
                let messages: Vec<String> = errors.iter().map(|err| {
                    if let Some(message) = &err.message {
                        message.to_string()
                    } else {
                        format!("Invalid value for {}", field)
                    }
                }).collect();
                messages.join(", ")
            }).collect::<Vec<String>>().join("; ");
            return Ok(render_as_json(&format!(r#"{{"error": "{}"}}"#, error_message),BAD_REQUEST_STATUS));
        }
    }

    match get_user_by_username(changeset.username.as_str()) {
        Ok(Some(_)) => {
            return Ok(render_as_json("Username already exists", BAD_REQUEST_STATUS));
        }
        Ok(None) => (),
        Err(e) => {
            return Ok(render_as_json(&format!(r#"{{"error": "Database error: {}"}}"#, e), BAD_REQUEST_STATUS));
        }
    }
    
    insert_user(&changeset)?;
    let json = serde_json::to_string(&changeset)?;

    Ok(render_as_json(json.as_str(), CREATED_STATUS))
}

