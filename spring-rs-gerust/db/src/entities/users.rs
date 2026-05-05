use bcrypt::{hash, DEFAULT_COST};
use chrono::Utc;
use sqlx::types::chrono::NaiveDateTime;
#[cfg(feature = "test-helpers")]
use fake::{faker::lorem::en::*, Dummy};
use serde::Deserialize;
use serde::Serialize;
use sqlx::{PgPool, Postgres};
use uuid::Uuid;
use validator::Validate;

#[derive(Serialize, Debug, Deserialize)]
pub struct User {
    pub id: Uuid,
    pub username: String,
    pub password: String,
    pub created_at: Option<NaiveDateTime>
}

#[derive(Deserialize, Serialize, Validate, Clone, Debug)]
#[cfg_attr(feature = "test-helpers", derive(Serialize, Dummy))]
pub struct UserChangeset {
    //#[cfg_attr(feature = "test-helpers", dummy(faker = "…()"))]
    //#[validate(…))]
    pub username: String,
    //#[cfg_attr(feature = "test-helpers", dummy(faker = "…()"))]
    //#[validate(…))]
    pub password: String,
}

pub async fn find_by_username(
    username: String,
    executor: impl sqlx::Executor<'_, Database = Postgres>,
) -> Result<User, crate::Error> {
    match sqlx::query_as!(
        User,
        "SELECT id, username,password,created_at FROM users WHERE username = $1",
        username
    )
        .fetch_optional(executor)
        .await
        .map_err(crate::Error::DbError)?
    {
        Some(user) => Ok(user),
        None => Err(crate::Error::NoRecordFound),
    }
}
pub async fn load_all(
    executor: impl sqlx::Executor<'_, Database = Postgres>,
) -> Result<Vec<User>, crate::Error> {
    let users = sqlx::query_as!(User, "SELECT id, username, password, created_at FROM users")
        .fetch_all(executor)
        .await?;
    Ok(users)
}

pub async fn load(
    id: Uuid,
    executor: impl sqlx::Executor<'_, Database = Postgres>,
) -> Result<User, crate::Error> {
    match sqlx::query_as!(
        User,
        "SELECT id, username, password, created_at FROM users WHERE id = $1",
        id
    )
    .fetch_optional(executor)
    .await
    .map_err(crate::Error::DbError)?
    {
        Some(user) => Ok(user),
        None => Err(crate::Error::NoRecordFound),
    }
}

pub async fn create(
    user: UserChangeset,
    executor: impl sqlx::Executor<'_, Database = Postgres>,
) -> Result<User, crate::Error> {
    user.validate()?;

    let record = sqlx::query!(
        "INSERT INTO users (username, password,created_at) VALUES ($1, $2, $3) RETURNING id, created_at",
        user.username,
        hash(&user.password, DEFAULT_COST).unwrap(),
        Utc::now().naive_utc(),
    )
    .fetch_one(executor)
    .await
    .map_err(crate::Error::DbError)?;

    Ok(User {
        id: record.id,
        username: user.username,
        password: user.password,
        created_at: record.created_at
    })
}

pub async fn update(
    id: Uuid,
    user: UserChangeset,
    executor: impl sqlx::Executor<'_, Database = Postgres>,
) -> Result<User, crate::Error> {
    user.validate()?;

    match sqlx::query!(
        "UPDATE users SET username = $1, password = $2 WHERE id = $3 RETURNING id,created_at",
        user.username,
        user.password,
        id
    )
    .fetch_optional(executor)
    .await
    .map_err(crate::Error::DbError)?
    {
        Some(record) => Ok(User {
            id: record.id,
            username: user.username,
            password: user.password,
            created_at: record.created_at

        }),
        None => Err(crate::Error::NoRecordFound),
    }
}

pub async fn delete(
    id: Uuid,
    executor: impl sqlx::Executor<'_, Database = Postgres>,
) -> Result<(), crate::Error> {
    match sqlx::query!("DELETE FROM users WHERE id = $1 RETURNING id", id)
        .fetch_optional(executor)
        .await
        .map_err(crate::Error::DbError)?
    {
        Some(_) => Ok(()),
        None => Err(crate::Error::NoRecordFound),
    }
}

