import {
  MsalAuthenticationTemplate,
  useMsal,
} from "@azure/msal-react";
import { InteractionType } from "@azure/msal-browser";
import { loginRequest, protectedResources } from "../authConfig.js";function ProtectedContent() {
  const { instance, accounts } = useMsal();

  const obtenerToken = async () => {
  try {
    const response = await instance.acquireTokenSilent({
      scopes: protectedResources.api.scopes,
      account: accounts[0],
    });

    const payload = JSON.parse(atob(response.accessToken.split('.')[1]));

    console.log("iss:", payload.iss);
    console.log("aud:", payload.aud);
    console.log("scp:", payload.scp);

    console.log("Access Token:", response.accessToken);
  } catch (error) {
    console.error("Error al obtener el token:", error);
  }
};

  return (
    <>
      <h2>Página protegida</h2>
      <p>Solo puedes ver esta página si estás autenticado.</p>

      <button onClick={obtenerToken}>
        Obtener Access Token
      </button>
    </>
  );
}

function ProtectedPage() {
  return (
    <MsalAuthenticationTemplate
      interactionType={InteractionType.Redirect}
      authenticationRequest={loginRequest}
    >
      <ProtectedContent />
    </MsalAuthenticationTemplate>
  );
}

export default ProtectedPage;