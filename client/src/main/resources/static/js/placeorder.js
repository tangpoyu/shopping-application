 document.addEventListener("DOMContentLoaded", function() {
            // Add an event listener to the submit button
            var submitButton = document.getElementById("submitButton");
            submitButton.addEventListener("click", function(event) {
                // Get a reference to the table element
                var cartBody = document.getElementById("cart-body");

                // Initialize an empty array to hold the selected rows
                var selectedRows = [];

                // Loop through each row in the cart
                for (var i = 0; i < cartBody.rows.length; i++) {
                    var checkbox = cartBody.rows[i].cells[0].getElementsByTagName("input")[0];
                    selectedRows.push(checkbox.checked);
                }

                // Submit the form with hidden data of array of selected rows
                var form = document.getElementById("myForm");
                var input = document.getElementById("selectedRows");
                input.value = selectedRows;
                form.submit();
            });
        });