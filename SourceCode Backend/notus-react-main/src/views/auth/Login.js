import React, { useState } from "react";
import { useHistory } from "react-router-dom";
import ceb from "../../assets/img/ceb-1.png";
import loginBackground from "../../assets/img/background4.png";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import { apiPath } from "../../config";

export default function Login() {
  const [userId, setuserId] = useState("");
  const [password, setPassword] = useState("");
  const [loginType, setLoginType] = useState("HR");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const history = useHistory();
  const { login } = useAuth();

  // Now accepts an explicit login type (from whichever button was clicked)
  // so the type-selection and the submit happen in a single click.
  const handleSubmit = async (e, selectedLoginType) => {
    e.preventDefault();
    if (isSubmitting) {
      return;
    }

    const currentLoginType = selectedLoginType ?? loginType;
    setLoginType(currentLoginType);
    setIsSubmitting(true);

    try {
      const response = await fetch(apiPath("/api/v1/secinfo/login"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          user_id: userId.trim(),
          password: password,
          login_type: currentLoginType,
        }),
      });

      const data = await response.json();

      if (data.authenticated && data.has_ncre_access) {
        // update auth context (persists session, permitted areas, flips isAuthenticated)
        try {
          login(data);
        } catch (err) {
          console.error(
            "Auth login helper failed, falling back to sessionStorage",
            err
          );
          // fallback
          if (data.session_id)
            sessionStorage.setItem("session_id", data.session_id);
          if (data.user_info) {
            sessionStorage.setItem("user_id", data.user_info.user_id);
            sessionStorage.setItem("user_name", data.user_info.user_name);
            sessionStorage.setItem(
              "user_category",
              data.user_info.user_category
            );
            sessionStorage.setItem(
              "region_code",
              data.user_info.region_code || ""
            );
            sessionStorage.setItem(
              "province_code",
              data.user_info.province_code || ""
            );
            sessionStorage.setItem("area_code", data.user_info.area_code || "");
            sessionStorage.setItem(
              "access_scope",
              data.user_info.access_scope || ""
            );
          }
        }

        toast.success(data.message || "Login successful");

        // No area prompt. The permitted areas were resolved server side, so the user
        // lands on the dashboard already loading data for every area they may see.
        history.push("/admin");
      } else if (data.authenticated && !data.has_ncre_access) {
        toast.warn(data.message || "Authentication successful, but no NCRE access is configured.");
      } else {
        toast.error(data.message || "Invalid EPF number or password.");
      }
    } catch (error) {
      console.error("Error:", error);
      toast.error("An error occurred during login");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      {/* Full-page background image (login page only) */}
      <div
        className="min-h-screen w-full flex flex-col justify-center py-4 px-4 sm:px-6 lg:px-8"
        style={{
          backgroundColor: "#f1f5f9",
          backgroundImage: `url(${loginBackground})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
        }}
      >
        {/* Card */}
        <div
          className="w-full relative mx-auto"
          style={{
            maxWidth: "420px",
            zIndex: 1,
            background: "rgba(255,255,255,0.88)",
            backdropFilter: "blur(14px)",
            WebkitBackdropFilter: "blur(14px)",
            borderRadius: "20px",
            boxShadow:
              "0 25px 60px rgba(0,0,0,0.18), 0 0 0 1px rgba(255,255,255,0.35)",
            overflow: "hidden",
          }}
        >
          {/* Top blue strip - changed from crimson */}
          <div style={{ height: "6px", background: "linear-gradient(90deg, #001a33, #003366, #001a33)" }} />

          <div className="px-8 py-6">
            {/* Logo */}
            <div className="flex flex-col items-center mb-4">
              <div className="logo-ring">
                <div
                  style={{
                    width: "100%",
                    height: "100%",
                    borderRadius: "50%",
                    background: "#fff",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    overflow: "hidden",
                  }}
                >
                  <img
                    alt="NSO logo"
                    src={ceb}
                    style={{ width: "90px", height: "90px", objectFit: "contain" }}
                  />
                </div>
              </div>
              <h1
                style={{
                  fontSize: "20px",
                  fontWeight: "700",
                  color: "#1a0000",
                  letterSpacing: "0.02em",
                  marginBottom: "2px",
                }}
              >
                Welcome Back
              </h1>
              <p style={{ fontSize: "13px", color: "#888", fontWeight: "400" }}>
                Sign in to your account
              </p>
            </div>

            {/* Form */}
            <form onSubmit={(e) => handleSubmit(e)}>
              {/* EPF Number */}
              <div className="mb-4">
                <label
                  htmlFor="userId"
                  style={{
                    display: "block",
                    fontSize: "12px",
                    fontWeight: "600",
                    color: "#555",
                    textTransform: "uppercase",
                    letterSpacing: "0.08em",
                    marginBottom: "4px",
                  }}
                >
                  EPF Number
                </label>
                <div style={{ position: "relative" }}>
                  <span
                    style={{
                      position: "absolute",
                      left: "14px",
                      top: "50%",
                      transform: "translateY(-50%)",
                      color: "#aaa",
                      fontSize: "16px",
                    }}
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                      <circle cx="12" cy="7" r="4" />
                    </svg>
                  </span>
                  <input
                    type="text"
                    id="userId"
                    placeholder="Enter your EPF Number"
                    value={userId}
                    onChange={(e) => setuserId(e.target.value)}
                    style={{
                      width: "100%",
                      padding: "10px 14px 10px 42px",
                      fontSize: "14px",
                      color: "#222",
                      background: "#f8f8f8",
                      border: "1.5px solid #e8e8e8",
                      borderRadius: "10px",
                      outline: "none",
                      transition: "border-color 0.2s, box-shadow 0.2s",
                      boxSizing: "border-box",
                    }}
                    onFocus={(e) => {
                      e.target.style.borderColor = "#002244";
                      e.target.style.boxShadow = "0 0 0 3px rgba(0,34,68,0.1)";
                      e.target.style.background = "#fff";
                    }}
                    onBlur={(e) => {
                      e.target.style.borderColor = "#e8e8e8";
                      e.target.style.boxShadow = "none";
                      e.target.style.background = "#f8f8f8";
                    }}
                  />
                </div>
              </div>

              {/* Password */}
              <div className="mb-4">
                <label
                  htmlFor="password"
                  style={{
                    display: "block",
                    fontSize: "12px",
                    fontWeight: "600",
                    color: "#555",
                    textTransform: "uppercase",
                    letterSpacing: "0.08em",
                    marginBottom: "4px",
                  }}
                >
                  Password
                </label>
                <div style={{ position: "relative" }}>
                  <span
                    style={{
                      position: "absolute",
                      left: "14px",
                      top: "50%",
                      transform: "translateY(-50%)",
                      color: "#aaa",
                    }}
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                    </svg>
                  </span>
                  <input
                    type={showPassword ? "text" : "password"}
                    id="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{
                      width: "100%",
                      padding: "10px 42px 10px 42px",
                      fontSize: "14px",
                      color: "#222",
                      background: "#f8f8f8",
                      border: "1.5px solid #e8e8e8",
                      borderRadius: "10px",
                      outline: "none",
                      transition: "border-color 0.2s, box-shadow 0.2s",
                      boxSizing: "border-box",
                    }}
                    onFocus={(e) => {
                      e.target.style.borderColor = "#002244";
                      e.target.style.boxShadow = "0 0 0 3px rgba(0,34,68,0.1)";
                      e.target.style.background = "#fff";
                    }}
                    onBlur={(e) => {
                      e.target.style.borderColor = "#e8e8e8";
                      e.target.style.boxShadow = "none";
                      e.target.style.background = "#f8f8f8";
                    }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                      position: "absolute",
                      right: "14px",
                      top: "50%",
                      transform: "translateY(-50%)",
                      background: "none",
                      border: "none",
                      cursor: "pointer",
                      color: "#aaa",
                      padding: "0",
                    }}
                  >
                    {showPassword ? (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                        <line x1="1" y1="1" x2="23" y2="23" />
                      </svg>
                    ) : (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              {/* Remember me */}
              <div className="flex items-center mb-5">
                <input
                  id="customCheckLogin"
                  type="checkbox"
                  style={{
                    width: "16px",
                    height: "16px",
                    accentColor: "#002244",
                    cursor: "pointer",
                  }}
                />
                <label
                  htmlFor="customCheckLogin"
                  style={{
                    marginLeft: "8px",
                    fontSize: "13px",
                    color: "#666",
                    cursor: "pointer",
                  }}
                >
                  Remember me
                </label>
              </div>

              {/* HR / AD Sign In - each button both selects the login type
                  AND submits the request in a single click */}
              <div className="mt-2 flex w-full flex-col gap-3 sm:flex-row">
                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={(e) => handleSubmit(e, "HR")}
                  style={{
                    flex: 1,
                    padding: "12px",
                    background: isSubmitting
                      ? "#5a6f85"
                      : "linear-gradient(135deg, #002244, #001122)",
                    color: "#fff",
                    fontWeight: "700",
                    fontSize: "13px",
                    letterSpacing: "0.06em",
                    border: "none",
                    borderRadius: "10px",
                    cursor: isSubmitting ? "not-allowed" : "pointer",
                    boxShadow: "0 6px 20px rgba(0,34,68,0.3)",
                    transition: "transform 0.15s, box-shadow 0.15s",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "8px",
                  }}
                  onMouseEnter={(e) => {
                    if (!isSubmitting) {
                      e.currentTarget.style.transform = "translateY(-1px)";
                      e.currentTarget.style.boxShadow = "0 10px 28px rgba(0,34,68,0.4)";
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!isSubmitting) {
                      e.currentTarget.style.transform = "translateY(0)";
                      e.currentTarget.style.boxShadow = "0 6px 20px rgba(0,34,68,0.3)";
                    }
                  }}
                >
                  {isSubmitting && loginType === "HR" ? (
                    <>
                      <Spinner />
                      SIGNING IN...
                    </>
                  ) : (
                    "HR SIGN IN"
                  )}
                </button>

                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={(e) => handleSubmit(e, "AD")}
                  style={{
                    flex: 1,
                    padding: "12px",
                    background: isSubmitting
                      ? "#5a6f85"
                      : "linear-gradient(135deg, #002244, #001122)",
                    color: "#fff",
                    fontWeight: "700",
                    fontSize: "13px",
                    letterSpacing: "0.06em",
                    border: "none",
                    borderRadius: "10px",
                    cursor: isSubmitting ? "not-allowed" : "pointer",
                    boxShadow: "0 6px 20px rgba(0,34,68,0.3)",
                    transition: "transform 0.15s, box-shadow 0.15s",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "8px",
                  }}
                  onMouseEnter={(e) => {
                    if (!isSubmitting) {
                      e.currentTarget.style.transform = "translateY(-1px)";
                      e.currentTarget.style.boxShadow = "0 10px 28px rgba(0,34,68,0.4)";
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!isSubmitting) {
                      e.currentTarget.style.transform = "translateY(0)";
                      e.currentTarget.style.boxShadow = "0 6px 20px rgba(0,34,68,0.3)";
                    }
                  }}
                >
                  {isSubmitting && loginType === "AD" ? (
                    <>
                      <Spinner />
                      SIGNING IN...
                    </>
                  ) : (
                    "AD SIGN IN"
                  )}
                </button>
              </div>
            </form>

            {/* Footer Info */}
            <div
              style={{
                textAlign: "center",
                marginTop: "16px",
                fontSize: "12px",
                color: "#888",
                lineHeight: "1.4",
              }}
            >
              © 2026 Utility Solutions & Automation Branch,<br />
              Electricity Distribution Lanka (Private) Limited.<br />
              All Rights Reserved &nbsp; Version 1.2.3
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

function Spinner() {
  return (
    <svg
      className="h-4 w-4 animate-spin"
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
      style={{ marginRight: "8px" }}
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"
      ></circle>
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
      ></path>
    </svg>
  );
}