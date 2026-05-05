pub mod users;

use serde::{Deserialize, Serialize};
use validator::Validate;

pub const INSERT_BOOKMARK: &str = "INSERT INTO bookmarks (url, title) VALUES (?, ?);";
pub const INSERT_USER: &str = "INSERT INTO User (username, password) VALUES (?, ?);";
pub const SELECT_USER_BY_USERNAME: &str = "SELECT id, username, password from User WHERE username= ?;";

pub const SELECT_ALL_BOOKMARKS: &str = "SELECT id, url, title FROM bookmarks ORDER BY created_at DESC;";


#[derive(Debug, Clone,Serialize, Deserialize)]
pub struct Bookmark {
    pub id: String,
    pub url: String,
    pub title: String,
}

mod tests {
    use super::*;
    #[test]
    fn test_bookmark_serialization() {
        let item = Some("23".to_string());
        
        let iterm = item.and_then(|x| x.parse::<i32>().ok()).unwrap_or(0);
    }
}