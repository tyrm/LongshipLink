use std::collections::HashMap;
use worker::*;
use pubnub::*;
use pubnub::access::permissions;
use serde_json::json;
use serde::Deserialize;

#[event(fetch)]
async fn main(req: Request, env: Env, _ctx: Context) -> Result<Response> {
    let router = Router::new();
    router
        .get_async("/api/v1/auth/user", authenticate_user)
        .run(req, env)
        .await
}

#[derive(Deserialize)]
struct AuthQuery {
    sid: Option<String>,
    secret: Option<String>,
    uid: Option<String>,
}

async fn authenticate_user(req: Request, ctx: RouteContext<()>) -> Result<Response> {
    let kv = ctx.env.kv("ll-dev")?;

    let pub_key = match ctx.env.var("PUBNUB_PUBLISH_KEY") {
        Ok(pub_key) => pub_key.to_string(),
        Err(e) => {
            console_debug!("Error reading PUBNUB_PUBLISH_KEY: {:?}", e);
            return Response::error("Service Unavailable", 503);
        }
    };
    let sub_key = match ctx.env.var("PUBNUB_SUBSCRIBE_KEY") {
        Ok(sub_key) => sub_key.to_string(),
        Err(e) => {
            console_debug!("Error reading PUBNUB_SUBSCRIBE_KEY: {:?}", e);
            return Response::error("Service Unavailable", 503);
        }
    };
    let sec_key = match ctx.env.var("PUBNUB_SECRET_KEY") {
        Ok(sec_key) => sec_key.to_string(),
        Err(e) => {
            console_debug!("Error reading PUBNUB_SECRET_KEY: {:?}", e);
            return Response::error("Service Unavailable", 503);
        }
    };
    console_debug!("PUBNUB_PUBLISH_KEY: {}, PUBNUB_SUBSCRIBE_KEY: {}, PUBNUB_SECRET_KEY: {}", pub_key.to_string(), sub_key.to_string(), sec_key.to_string());

    let query: AuthQuery = req.query()?;

    // Return 400 error if any of the required parameters are missing
    if query.sid.is_none() || query.secret.is_none() || query.uid.is_none() {
        return Response::error("Missing required parameter", 400);
    }

    let sid = match query.sid {
        Some(sid) => sid,
        None => {
            return Response::error("Missing required parameter", 400);
        }
    };
    let secret = match query.secret {
        Some(secret) => secret,
        None => {
            return Response::error("Missing required parameter", 400);
        }
    };
    let uid = match query.uid {
        Some(uid) => uid,
        None => {
            return Response::error("Missing required parameter", 400);
        }
    };

    // read server secret by sid
    let secret_key = format!("server.{}.secret", sid);
    let kv_secret_result = kv.get(&secret_key.to_string()).text().await;
    let kv_secret = match kv_secret_result {
        Ok(Some(secret)) => secret,
        Ok(None) => {
            console_debug!("Secret not found in KV: {}", secret_key);
            return Response::error("Unauthorized", 401);
        },
        Err(error) => {
            console_error!("Error reading secret from KV: {:?}", error);
            return Response::error("Internal Server Error", 500);
        }
    };
    console_debug!("kv_key: {}, kv_secret: {}", secret_key.to_string(), kv_secret.to_string());

    // Compare the secret from the request with the secret from KV
    if secret != kv_secret {
        console_debug!("Secret mismatch: {} != {}", secret.to_string(), kv_secret.to_string());
        return Response::error("Unauthorized", 401);
    };

    // Validate the player using PlayerDB
    if !validate_player(uid.to_string()).await? {
        return Response::error("Invalid player id", 400);
    }

    // Get PubNub keys
    let token = match get_pn_token(uid.to_string(), pub_key.to_string(), sub_key.to_string(), sec_key.to_string()).await {
        Ok(token) => token,
        Err(error) => {
            console_error!("Error getting PubNub token: {:?}", error);
            return Response::error("Internal Server Error", 500);
        }
    };

    // Create a JSON object
    let params = json!({
        "sub_key": sub_key.to_string(),
        "pub_key": pub_key.to_string(),
        "token": token.to_string(),
    });
    Response::ok(params.to_string())
}

async fn validate_player(uid: String) -> Result<bool> {
    let url = format!("https://playerdb.co/api/player/minecraft/{}", uid);
    let http_response = match reqwest::get(url).await {
        Ok(response) => response,
        Err(error) => {
            error.to_string();
            console_error!("Error fetching data: {:?}", error);
            return Err(Error::from(error.to_string()));
        }
    };

    let status = http_response.status();
    console_debug!("Status: {}", status);
    if status != 200 {
        return Ok(false);
    }

    return Ok(true);
}

async fn get_pn_token(uid: String, pub_key: String, sub_key: String, sec_key: String) -> Result<String> {
    let client = PubNubClientBuilder::with_reqwest_transport()
        .with_keyset(Keyset {
            publish_key: Some(pub_key),
            subscribe_key: sub_key,
            secret_key: Some(sec_key),
        })
        .with_user_id("auth-server")
        .build();

    let mut client = match client {
        Ok(client) => client,
        Err(error) => {
            console_error!("Error creating PubNub client: {:?}", error);
            return Err(Error::from(error.to_string()));
        }
    };

    let grant_result = client
        .grant_token(60)
        .authorized_user_id(uid.to_string())
        .resources(&[
            permissions::channel("global").read().write().get(),
            permissions::channel("global-pnpres").read().get(),
            permissions::channel(format!("tell_{}", uid)).read().write(),
            permissions::user_id(uid.to_string()).update().delete(),
        ])
        .patterns(&[
            permissions::channel("^tell_[0-9a-fA-F-]+$").write(),
            permissions::user_id(".*").get(),
        ])
        .execute().await;

    let grant_result = match grant_result {
        Ok(result) => result,
        Err(error) => {
            console_error!("Error granting token: {:?}", error);
            return Err(Error::from(error.to_string()));
        }
    };

    Ok(grant_result.token.clone())
}
