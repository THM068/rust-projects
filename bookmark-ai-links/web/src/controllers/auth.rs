use std::future::Future;
use anyhow::{anyhow, Error};
use bcrypt::verify;
use jwt_simple::algorithms::{HS256Key, MACLike};
use jwt_simple::claims::{Claims, JWTClaims};
use jwt_simple::prelude::{Deserialize, Duration, Serialize};
use crate::controllers::{render_as_json, BAD_REQUEST_STATUS, INTERNAL_SERVER_ERROR_STATUS, OK_STATUS, UNAUTHORIZED_STATUS};
use crate::models::users::get_user_by_username;
use spin_sdk::http::{IntoResponse, Params, Request, Response};

const SECRET: &[u8] = b"your-256-bit-secret";
pub async fn handle_login(req: Request, _params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    
    let user_changeset: crate::controllers::user::UserChangeset = match serde_json::from_slice(req.body()) {
        Ok(data) => data,
        Err(e) => {
            return Ok(render_as_json(&format!(r#"{{"error": "Invalid JSON: missing username or password"}}"#), BAD_REQUEST_STATUS));
        }
    };
    
    let user_in_db = match get_user_by_username(&user_changeset.username.as_str()) {
        Ok(Some(user)) => user,
        Ok(None) => {
            return Ok(render_as_json(&format!(r#"{{"error": "Invalid username or password"}}"#), UNAUTHORIZED_STATUS));
        }
        Err(e) => {
            return Ok(render_as_json(&format!(r#"{{"error": "Database error: unexepected error"}}"#), INTERNAL_SERVER_ERROR_STATUS));
        }
    };

    if !verify_password(&user_changeset.password, &user_in_db.password) {
        return Ok(render_as_json(&format!(r#"{{"error": "Invalid username or password"}}"#), UNAUTHORIZED_STATUS));
    }

    let key = HS256Key::from_bytes(SECRET);
    let user_claims = UserClaims {
        user_id: user_in_db.id,
        username: user_in_db.username.clone(),
    };
    
    let claims = Claims::with_custom_claims(user_claims, Duration::from_hours(2))
        .with_subject(&user_in_db.username);
        
    let token = key.authenticate(claims)?;
    
    let json_web_token = JsonWebToken { token: token.to_string() };
    let json = serde_json::to_string(&json_web_token)?;
    
    Ok(render_as_json(json.as_str(), OK_STATUS))
}



#[derive(serde::Serialize, serde::Deserialize)]
struct JsonWebToken {
    token: String,
}

pub fn verify_password(password: &str, hash: &str) -> bool {
    verify(password, hash).unwrap()
}



#[derive(Serialize, Deserialize)]
pub struct UserClaims {
    pub user_id: u64,
    pub username: String,
}

fn verify_jwt(req: &Request) -> Result<JWTClaims<UserClaims>, anyhow::Error> {
    // Extract Authorization header
    let auth_header = req
        .header("authorization")
        .ok_or_else(|| anyhow!("Missing Authorization header"))?
        .as_str()
        .ok_or_else(|| anyhow!("Invalid Authorization header"))?;

    // Extract token from "Bearer <token>"
    let token = auth_header
        .strip_prefix("Bearer ")
        .ok_or_else(|| anyhow!("Invalid Authorization format"))?;

    // Verify token
    let key = HS256Key::from_bytes(b"your-secret-key");
    key.verify_token::<UserClaims>(token, None)
        .map_err(|e| anyhow!("Invalid token: {}", e))
}

fn unauthorized_response(message: &str) -> impl IntoResponse {
    Response::builder()
        .status(401)
        .header("content-type", "application/json")
        .body(format!(r#"{{"error": "{}"}}"#, message))
        .build()
}

#[macro_export] macro_rules!  protected_route {
    ($req:expr, $handler:expr) => {{
        match verify_jwt(&$req) {
            Ok(claims) => $handler(claims),
            Err(e) => Ok(
                spin_sdk::http::Response::builder()
                    .status(401)
                    .header("content-type", "application/json")
                    .body(format!(r#"{{"error": "Unauthorized: {}"}}"#, e))
                    .build()
            ),
        }
    }};
}

pub struct Auth;

impl Auth {
    const SECRET_KEY: &'static [u8] = b"your-256-bit-secret";

    pub fn verify(req: &Request) -> Result<JWTClaims<UserClaims>, anyhow::Error> {
        let auth_header = req
            .header("authorization")
            .and_then(|h| h.as_str())
            .ok_or_else(|| anyhow::anyhow!("Missing Authorization header"))?;

        let token = auth_header
            .strip_prefix("Bearer ")
            .ok_or_else(|| anyhow::anyhow!("Invalid Authorization format"))?;

        let key = HS256Key::from_bytes(Self::SECRET_KEY);
        key.verify_token::<UserClaims>(token, None)
            .map_err(|e| anyhow::anyhow!("Invalid token: {}", e))
    }
}

