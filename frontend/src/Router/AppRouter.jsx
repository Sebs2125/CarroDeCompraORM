import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "../pages/Home";
import Login from "../pages/Login";
import Carrito from "../pages/Carrito";
import AdminProducts from "../pages/AdminProducts";
import Adminproductosfrom from "../pages/AdminProductoForm";



function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/carrito" element={<Carrito />} />

                {/* Admin */}
                <Route path="/admin/productos" element={<AdminProducts />} />
                <Route path="/admin/productos/nuevo" element={<AdminProductoForm />} />

            </Routes>
        </BrowserRouter>
    );
}

export default AppRouter;