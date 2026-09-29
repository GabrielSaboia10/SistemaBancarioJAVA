/**
 * Web: a API fica no mesmo domínio (proxy do `ng serve` em dev, nginx em produção),
 * então as chamadas usam caminhos relativos como /api/contas.
 */
export const environment = {
  apiUrl: '',
};
