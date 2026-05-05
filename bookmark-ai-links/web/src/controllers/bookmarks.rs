use askama::Template;
use crate::controllers::{redirect_to, render_as_html};
use crate::models::{Bookmark, INSERT_BOOKMARK, SELECT_ALL_BOOKMARKS};
use serde::{Deserialize, Serialize};
use spin_sdk::http::{IntoResponse, Params, Request};
use spin_sdk::sqlite::{Connection, Value};
use validator::Validate;

pub fn handle_add_bookmark(
    req: Request,
    params: Params,
) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    println!("Params: {:?}", params);

    let bookmark: BookmarkChangeset = serde_urlencoded::from_bytes(req.body().as_ref())?;

    println!("Bookmark: {:?}", bookmark);
    println!("SQL: '{}'", INSERT_BOOKMARK);
    println!("SQL length: {}", INSERT_BOOKMARK.len());
    let connection = Connection::open_default()?;
    let values = [Value::Text(bookmark.url.clone()), Value::Text( bookmark.title.clone())];

    connection.execute(INSERT_BOOKMARK, &values)?;
    // Here you would typically save the bookmark to a database
    // For now, we'll just redirect back to the home page
    Ok(redirect_to("/"))
}

pub fn handle_get_all_bookmarks(req: Request, params: Params) -> anyhow::Result<impl IntoResponse, anyhow::Error> {
    let connection = Connection::open_default()?;
    let values = [];
    let  stmt = connection.execute(SELECT_ALL_BOOKMARKS, values.as_slice())?;
    
    let bookmarks: Vec<Bookmark> = stmt
        .rows()
        .map(|row| Bookmark {
            id: row.get:: < i64>("id").unwrap_or(0).to_string(),
            url: row.get:: < & str>("url").unwrap_or_default().to_string(),
            title: row.get:: < & str>("title").unwrap_or_default().to_string(),
        })
        .collect();
    let template = BookMarksTemplate { bookmarks: bookmarks };

    Ok(render_as_html(&template.render()?))
}

#[derive(Debug, Serialize, Deserialize, Validate)]
struct BookmarkChangeset {
    #[validate(length(min = 1, message = "Title cannot be empty"))]
    title: String,
    
    #[validate(url(message = "Invalid URL format"))]
    url: String,
}

#[derive(Template)]
#[template(path = "bookmarks.html")]
pub struct BookMarksTemplate {
    pub bookmarks: Vec<Bookmark>,
}
