use bcrypt::{hash, DEFAULT_COST};
use serde::{Deserialize, Serialize};
use spin_sdk::sqlite::{Connection, QueryResult, Value};
use validator::Validate;
use crate::controllers::user::UserChangeset;
use crate::models::{INSERT_BOOKMARK, INSERT_USER, SELECT_USER_BY_USERNAME};

pub fn insert_user(userChangeset: &UserChangeset) -> anyhow::Result<(), anyhow::Error> {
    let connection = Connection::open_default()?;
    let values = [
        Value::Text(userChangeset.username.to_string()), 
        Value::Text(hash(userChangeset.password.to_string(), DEFAULT_COST).unwrap())
    ];
    //hash(&user.password, DEFAULT_COST).unwrap()
    let _ = connection.execute("BEGIN TRANSACTION;", &[]);
    connection.execute(INSERT_USER, &values)?;
    let _ = connection.execute("END TRANSACTION;", &[]);

    Ok(())
}

pub fn get_user_by_username(username: &str) -> anyhow::Result<Option<User>, anyhow::Error> {
    let connection = Connection::open_default()?;
    let values = [Value::Text(username.to_string())];
    let  mut stmt = connection.execute(SELECT_USER_BY_USERNAME, &values)?;
    
    let user_opt: Vec<User> = stmt
        .rows()
        .map(|row| User {
            id: row.get::<u64>("id").unwrap_or_default(),
            username: row.get::<&str>("username").unwrap_or_default().to_string(),
            password: row.get::<&str>("password").unwrap_or_default().to_string(),
        }).collect();
    
    if user_opt.is_empty() {
        return Ok(None);
    }
    Ok(user_opt.first().cloned())
    
}

#[derive(Debug, Serialize, Deserialize, Validate, Clone)]
pub struct User {
    pub id: u64,
    pub username: String,
    #[serde(skip_serializing)] // Exclude password from serialization for security reasons
    pub password: String,
    
}