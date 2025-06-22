package no.realitylab.arface.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import no.realitylab.arface.R
import no.realitylab.arface.data.remote.RetrofitClient
import no.realitylab.arface.data.request.UpdateProfileRequest
import no.realitylab.arface.data.response.UserResponse
import no.realitylab.arface.ui.auth.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView

    private lateinit var editName: EditText
    private lateinit var editPhone: EditText
    private lateinit var editAddress: EditText

    private lateinit var btnEdit: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnLogout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvUsername = view.findViewById(R.id.username)
        tvEmail = view.findViewById(R.id.email)
        tvPhone = view.findViewById(R.id.phone)
        tvAddress = view.findViewById(R.id.address)

        editName = view.findViewById(R.id.edit_name)
        editPhone = view.findViewById(R.id.edit_phone)
        editAddress = view.findViewById(R.id.edit_address)

        btnEdit = view.findViewById(R.id.btn_edit)
        btnSave = view.findViewById(R.id.btn_save)
        btnLogout = view.findViewById(R.id.btn_logout)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Có") { _, _ ->
                    logoutUser()
                }
                .setNegativeButton("Không", null)
                .show()
        }


        btnEdit.setOnClickListener {
            toggleEditMode(true)
        }

        btnSave.setOnClickListener {
            saveProfileUpdates()
        }

        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val sharedPreferences = requireActivity().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val bearerToken = "Bearer $token"

        RetrofitClient.instance.getUserProfile(bearerToken)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()?.data
                        user?.let {
                            tvUsername.text = it.name
                            tvEmail.text = it.email
                            tvPhone.text = it.contact
                            tvAddress.text = it.address
                        }
                    } else {
                        Toast.makeText(requireContext(), "Lỗi khi lấy thông tin: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun toggleEditMode(editing: Boolean) {
        if (editing) {
            editName.setText(tvUsername.text)
            editPhone.setText(tvPhone.text)
            editAddress.setText(tvAddress.text)

            tvUsername.visibility = View.GONE
            tvPhone.visibility = View.GONE
            tvAddress.visibility = View.GONE

            editName.visibility = View.VISIBLE
            editPhone.visibility = View.VISIBLE
            editAddress.visibility = View.VISIBLE

            btnSave.visibility = View.VISIBLE
        } else {
            tvUsername.visibility = View.VISIBLE
            tvPhone.visibility = View.VISIBLE
            tvAddress.visibility = View.VISIBLE

            editName.visibility = View.GONE
            editPhone.visibility = View.GONE
            editAddress.visibility = View.GONE

            btnSave.visibility = View.GONE
        }
    }

    private fun saveProfileUpdates() {
        val name = editName.text.toString()
        val contact = editPhone.text.toString()
        val address = editAddress.text.toString()

        val sharedPreferences = requireActivity().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val bearerToken = "Bearer $token"
        val request = UpdateProfileRequest(name, contact, address)

        RetrofitClient.instance.updateProfile(bearerToken, request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        tvUsername.text = name
                        tvPhone.text = contact
                        tvAddress.text = address

                        toggleEditMode(false)

                        Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Lỗi cập nhật: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun logoutUser() {
        val sharedPreferences = requireActivity().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finishAffinity()

        Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
    }
}
