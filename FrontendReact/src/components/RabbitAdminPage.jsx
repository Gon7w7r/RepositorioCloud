import { MsalAuthenticationTemplate } from "@azure/msal-react";
import { InteractionType } from "@azure/msal-browser";
import { Link } from "react-router-dom";
import { loginRequest } from "../authConfig.js";
import RabbitAdminPanel from "./RabbitAdminPanel";

export default function RabbitAdminPage() {
  return (
    <MsalAuthenticationTemplate
      interactionType={InteractionType.Redirect}
      authenticationRequest={loginRequest}
    >
      <Link to="/"><button>Volver</button></Link>
      <RabbitAdminPanel />
    </MsalAuthenticationTemplate>
  );
}