import { api } from "./api";

export const getProductos = () => api.get("/productos");
