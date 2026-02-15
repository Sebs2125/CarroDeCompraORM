import { useContext } from "react";
import { CarritoContext } from "../Context/CarritoContext";

export default function Carrito() {
    const { carrito, removeItem, clearCart } = useContext(CarritoContext);

    const totalGeneral = carrito.reduce(
        (acc, item) => acc + item.precio * item.cantidad,
        0
    );

    return (
        <div className="max-w-5xl mx-auto p-6">

            <h1 className="text-3xl font-bold text-center mb-8">Carrito de Compra</h1>

            {/* Datos del Cliente */}
            <div className="bg-white border rounded-lg p-5 mb-8">
                <h2 className="text-xl font-semibold mb-3">Datos del Cliente</h2>

                <label className="font-medium">Nombre del Cliente</label>
                <input
                    className="w-full border p-2 rounded mt-2"
                    placeholder="Ingrese el nombre"
                />
            </div>

            {/* Tabla */}
            <div className="bg-white border rounded-lg p-5">
                <table className="w-full border-collapse text-left">
                    <thead>
                        <tr className="border-b font-semibold">
                            <th className="p-3">Producto</th>
                            <th className="p-3">Precio</th>
                            <th className="p-3">Cantidad</th>
                            <th className="p-3">Total</th>
                            <th className="p-3">Acción</th>
                        </tr>
                    </thead>

                    <tbody>
                        {carrito.map((item) => (
                            <tr key={item.id} className="border-b">
                                <td className="p-3">{item.nombre}</td>
                                <td className="p-3">{item.precio.toLocaleString()}</td>
                                <td className="p-3">{item.cantidad}</td>
                                <td className="p-3">
                                    {(item.precio * item.cantidad).toLocaleString()}
                                </td>
                                <td className="p-3">
                                    <button
                                        onClick={() => removeItem(item.id)}
                                        className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
                                    >
                                        Eliminar
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                {/* Total */}
                <div className="text-right text-xl font-bold mt-4">
                    Total: {totalGeneral.toLocaleString()} RD$
                </div>
            </div>

            {/* Botones */}
            <div className="flex justify-center gap-4 mt-6">
                <button className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">
                    Procesar Compra
                </button>

                <button
                    onClick={clearCart}
                    className="bg-gray-300 px-6 py-2 rounded hover:bg-gray-400"
                >
                    Limpiar Carro
                </button>
            </div>
        </div>
    );
}
